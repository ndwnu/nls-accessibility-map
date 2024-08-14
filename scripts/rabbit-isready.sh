RUN=0
until docker compose exec -T nls-rabbitmq rabbitmq-diagnostics is_running; do
  echo "Hunting for a Rabbit mq, but it's not ready yet... sleeping another second.."

  if [ $RUN -eq 0 ]
  then
    base64 -d <<< "H4sIAAAAAAAAA1VQQQ7DMAi79xXc3EoB7tlXkNg/1sfPkGzaUBuBAWMQKTPa0Q74jHlsjxaWmcx+YtNy0nDAgmlnqwLPYM/sIq8uBkU4q+W86vOKzxQTgZIEU7YZurY672TuO5sO5ywZ6zdk/CXsziwV6fMHdvnaDMN4YG2k5h/iV9zXRRTUIxg5tOtUS4sTNs+aSSybdFbhABR1gVpXAnvhsL3DUu6mVdxI83tpv4FYy8Ccc/alLI91507Asg6+D9IQud9ZH75wpgEAAA==" | gunzip
  fi
  sleep 1
  RUN=1
done
