#!/bin/bash
# This script is used to run the artemis MQ broker in a container

# define these variables before running the script
#export POD_CONTAINER = podman | docker
export POD_CONTAINER="${POD_CONTAINER:-podman}"

echo "Pod container: $POD_CONTAINER"

#podman run --rm ${EXTRA_CONTAINER_OPTION} --name opentelemetry -p 1888:1888 -p 13133:13133 -p 4317:4317 -p 4318:4318 -p 55679:55679 --volume opentelemetry:/etc/otelcol --replace --pull=always otel/opentelemetry-collector

podman run --rm ${EXTRA_CONTAINER_OPTION} --name jaeger -p 5778:5778 -p 16686:16686 -p 4317:4317 -p 4318:4318 -p 14250:14250 -p 14268:14268 -p 9411:9411 jaegertracing/jaeger:2.0.0 --set receivers.otlp.protocols.http.endpoint=0.0.0.0:4318 --set receivers.otlp.protocols.grpc.endpoint=0.0.0.0:4317
