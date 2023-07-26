FROM busybox

ARG LOCAL_PATH

ENV TINI_VERSION v0.19.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini

COPY ${LOCAL_PATH} tgtg 
ENTRYPOINT ["/tini", "--", "/tgtg"]
