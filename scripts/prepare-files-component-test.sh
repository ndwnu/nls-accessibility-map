echo "Preparing files so backend api can start"

# Go to working directory if set as first argument
workingDir=$@;
if [[ -n "$workingDir" ]]; then
  cd $workingDir
fi

cd .tmp/trafficsigns
if [[ ! -e active ]]; then
  echo "[]" > test
  ln -s test active
  echo "Setup empty traffic sign cache"
fi

cd ../graphhopper
if [[ ! -e accessibility_latest ]]; then
  mkdir accessibility_latest
  cd accessibility_latest
  cp -R ../../../scripts/graphhopper-empty/* .
  echo "Setup empty graphhopper"
fi

echo "Done"