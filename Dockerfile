FROM busybox

WORKDIR /opt/app

ARG LOCAL_PATH
COPY ${LOCAL_PATH} tgtg

ENTRYPOINT ["/opt/app/tgtg"]
