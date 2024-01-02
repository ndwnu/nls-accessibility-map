until docker-compose exec -T nls-postgres pg_isready; do
  echo "Database not ready yet... sleeping another second.."
  sleep 1
done