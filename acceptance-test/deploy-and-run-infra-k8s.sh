#!/usr/bin/env bash
set -Eeuo pipefail


# AKS
KUBE_CONTEXT="nls-aks-pipeline-cluster"

# ACR
ACR_NAME="ndwnls"
ACR_LOGIN_SERVER="ndwnls.azurecr.io"

############################################
# CONFIGURATION (override via env vars)
##########################################

# Helm
HELM_RELEASE_NAME="${HELM_RELEASE_NAME:-nls-accessibility-map-api}"
HELM_CHART_PATH="${HELM_CHART_PATH:-./k8s}"

# Maven
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MAIN_POM_FILE="${MAIN_POM_FILE:-${SCRIPT_DIR}/../pom.xml}"

# Images (format: imageName=contextPath)
IMAGE_MAP="${IMAGE_MAP:-nls-accessibility-map-api=../docker/nls-accessibility-map-api nls-accessibility-map-job=../docker/nls-accessibility-map-job}"

# Docker control
SKIP_DOCKER_BUILD="${SKIP_DOCKER_BUILD:-false}"
PREDEFINED_IMAGE_TAG=${PREDEFINED_IMAGE_TAG:-}

#to avoid container port conflicts usage service-name-a:8080:8888 service-name-b:8080:8888
PORT_OVERRIDE_MAP="${PORT_OVERRIDE_MAP:-wiremock:8080:8888}"
############################################
# DERIVED VALUES
############################################

if [[ -n "${PREDEFINED_IMAGE_TAG}" ]]; then
  UNIQUE_TAG="${PREDEFINED_IMAGE_TAG}"
  DELETE_IMAGES=false
else
  UNIQUE_TAG="$(date +%s)"
  DELETE_IMAGES=true
fi

if [[ "${SKIP_DOCKER_BUILD}" == "true" ]]; then
  DELETE_IMAGES=false
fi
UNIQUE_NAMESPACE=$(echo "$UNIQUE_TAG" | sed 's/\.//g')
NAMESPACE="debug-nls-accessibility-map-${UNIQUE_NAMESPACE}"
PIDS=()

############################################
# CLEANUP HANDLER
############################################

cleanup() {
  echo ""
  echo "Cleaning up..."
  if [[ "$(uname)" == "Darwin" ]]; then
     sed -i '' "s/${UNIQUE_TAG}/@docker.image.tag@/g" "${HELM_CHART_PATH}/Chart.yaml"
  else
     sed -i "s/${UNIQUE_TAG}/@docker.image.tag@/g" "${HELM_CHART_PATH}/Chart.yaml"
  fi

  for PID in "${PIDS[@]:-}"; do
    kill "${PID}" 2>/dev/null || true
  done

  kubectl delete namespace "${NAMESPACE}" --wait=true 2>/dev/null || true

  if [[ "${DELETE_IMAGES}" == "true" ]]; then
    echo "Deleting temporary images from ACR..."
    for ENTRY in ${IMAGE_MAP}; do
      IMAGE_NAME="${ENTRY%%=*}"
      az acr repository delete \
        --name "${ACR_NAME}" \
        --image "${IMAGE_NAME}:${UNIQUE_TAG}" \
        --yes 2>/dev/null || true
    done
  else
    echo "Skipping image deletion."
  fi

  echo "Done."
}

trap cleanup EXIT

############################################
# VALIDATION
############################################

if [[ ! -f "${MAIN_POM_FILE}" ]]; then
  echo "ERROR: POM file not found: ${MAIN_POM_FILE}"
  exit 1
fi

############################################
# LOGIN & CLUSTER CONFIG
############################################

if [[ -n "${KUBE_CONTEXT}" ]]; then
  kubectl config use-context "${KUBE_CONTEXT}"
fi

az acr login --name "${ACR_NAME}"

############################################
# BUILD MAVEN PROJECT
############################################

echo "Building Maven project..."
mvn -f "${MAIN_POM_FILE}" clean package -DskipTests

############################################
# BUILD & PUSH DOCKER IMAGES
############################################

if [[ "${SKIP_DOCKER_BUILD}" != "true" ]]; then
  for ENTRY in ${IMAGE_MAP}; do
    IMAGE_NAME="${ENTRY%%=*}"
    CONTEXT_PATH="${ENTRY#*=}"
    FULL_IMAGE="${ACR_LOGIN_SERVER}/${IMAGE_NAME}:${UNIQUE_TAG}"

    echo "Building ${FULL_IMAGE} (context: ${CONTEXT_PATH})"
    # multi platform image from mac m2
    docker buildx build -t "${FULL_IMAGE}" "${CONTEXT_PATH}" --platform linux/amd64

    echo "Pushing ${FULL_IMAGE}"
    docker push "${FULL_IMAGE}"
  done
else
  echo "Skipping docker build & push."
fi

############################################
# CREATE NAMESPACE
############################################

kubectl create namespace "${NAMESPACE}"

############################################
# HELM VALUES
############################################
HELM_SET_ARGS=""
HELM_SET_ARGS+=" --set hostSuffix=${NAMESPACE}.svc.cluster.local"
HELM_SET_ARGS+=" --set buildNumber=${UNIQUE_TAG}"
HELM_SET_ARGS+=" --set nameSpace=${UNIQUE_TAG}"

if [[ "$(uname)" == "Darwin" ]]; then
   sed -i '' "s/@docker.image.tag@/${UNIQUE_TAG}/g" "${HELM_CHART_PATH}/Chart.yaml"
else
   sed -i "s/@docker.image.tag@/${UNIQUE_TAG}/g" "${HELM_CHART_PATH}/Chart.yaml"
fi

############################################
# DEPLOY
############################################

helm upgrade --install "${HELM_RELEASE_NAME}" "${HELM_CHART_PATH}" \
  --namespace "${NAMESPACE}" \
  ${HELM_SET_ARGS}

echo "Waiting for all long-running pods..."
kubectl wait --for=condition=ready pod \
  --selector='!job-name' \
  --namespace "${NAMESPACE}" \
  --timeout=180s || true

echo "Waiting for all Jobs to complete..."
for JOB in $(kubectl get jobs -n "${NAMESPACE}" -o jsonpath='{.items[*].metadata.name}'); do
  echo "Waiting for job ${JOB}..."
  kubectl wait --for=condition=complete job/"${JOB}" -n "${NAMESPACE}" --timeout=300s || true
done

############################################
# PORT FORWARDING
############################################
echo "Setting up pod-based port forwarding with deployment port overrides..."

# Parse PORT_OVERRIDE_MAP into associative array
declare -A PORT_MAPPING
IFS=',' read -ra ENTRIES <<< "$PORT_OVERRIDE_MAP"
for ENTRY in "${ENTRIES[@]}"; do
  DEPLOYMENT_NAME=$(echo "$ENTRY" | cut -d':' -f1)
  CONTAINER_PORT=$(echo "$ENTRY" | cut -d':' -f2)
  LOCAL_PORT=$(echo "$ENTRY" | cut -d':' -f3)
  PORT_MAPPING["$DEPLOYMENT_NAME:$CONTAINER_PORT"]=$LOCAL_PORT
done

# Forward first pod of each deployment
DEPLOYMENTS=$(kubectl get deployments -n "${NAMESPACE}" -o jsonpath='{.items[*].metadata.name}')

for DEPLOY in $DEPLOYMENTS; do
  # Get first pod of the deployment
  POD=$(kubectl get pods -n "${NAMESPACE}" -l "app=${DEPLOY}" -o jsonpath='{.items[0].metadata.name}')
  if [[ -z "$POD" ]]; then
    echo "No pods found for deployment $DEPLOY, skipping port forward"
    continue
  fi

  # Get container ports
  CONTAINER_PORTS=$(kubectl get pod "$POD" -n "${NAMESPACE}" -o jsonpath='{.spec.containers[0].ports[*].containerPort}')

  for PORT in $CONTAINER_PORTS; do
    LOCAL_PORT=${PORT_MAPPING["$DEPLOY:$PORT"]:-$PORT}  # default 1:1
    echo "Forwarding pod $POD:$PORT -> localhost:$LOCAL_PORT"
    kubectl port-forward pod/"$POD" "$LOCAL_PORT:$PORT" -n "${NAMESPACE}" &
    PIDS+=($!)
  done

done


############################################
# WAIT FOR USER
############################################

echo ""
echo "============================================"
echo "Environment Ready"
echo "Namespace: ${NAMESPACE}"
echo "Image Tag: ${UNIQUE_TAG}"
echo "Press Ctrl+X to cleanup and exit..."
echo "============================================"

# Read Ctrl+X (ASCII 24)
while true; do
  read -rsn1 key  # -r: raw input, -s: silent, -n1: 1 char
  if [[ $key == $'\x18' ]]; then
    echo "Ctrl+X detected. Cleaning up..."
    break
  fi
done
