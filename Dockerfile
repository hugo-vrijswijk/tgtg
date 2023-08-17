FROM busybox

WORKDIR /opt/app

ENV TINI_VERSION v0.19.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini

ARG LOCAL_PATH
COPY ${LOCAL_PATH} tgtg

ENTRYPOINT ["/tini", "--", "/opt/app/tgtg"]
