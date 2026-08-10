FROM ubuntu:latest
LABEL authors="azusky"

ENTRYPOINT ["top", "-b"]