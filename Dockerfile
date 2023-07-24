FROM virtuslab/scala-cli as builder
WORKDIR /app
COPY . .
RUN scala-cli --power package . --native-image -o tgtg
RUN chmod +x ./tgtg

FROM scratch
COPY --from=builder /app/tgtg /tgtg
ENTRYPOINT ["/tgtg"]
