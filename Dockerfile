FROM virtuslab/scala-cli as builder
WORKDIR /app
COPY . .
RUN scala-cli --power package . --native-image -o tgtg
RUN chmod +x ./tgtg

FROM scratch
WORKDIR /app
COPY --from=builder /app/tgtg /app/tgtg
ENTRYPOINT ["/app/tgtg"]
