FROM debian:stable-slim AS build-env

FROM virtuslab/scala-cli as builder
WORKDIR /app
COPY --from=build-env /lib/x86_64-linux-gnu/libz.so.1 /lib/x86_64-linux-gnu/libz.so.1
COPY . .
RUN scala-cli --power package . --native-image -o tgtg
RUN chmod +x ./tgtg

FROM gcr.io/distroless/base-debian10
WORKDIR /app
COPY --from=build-env /lib/x86_64-linux-gnu/libz.so.1 /lib/x86_64-linux-gnu/libz.so.1
COPY --from=builder /app/tgtg /app/tgtg
ENTRYPOINT ["/app/tgtg"]
