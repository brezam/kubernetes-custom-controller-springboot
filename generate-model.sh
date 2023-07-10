#!/usr/bin/env bash

# This script uses a docker image that uses kind (https://kind.sigs.k8s.io/)
# If you already have a kind configured cluster named "kind" (default), then a conflict will happen due to the equal names.
# Before running this script make sure you have executed the following command:
# docker pull ghcr.io/kubernetes-client/java/crd-model-gen:v1.0.6

scriptDir=$(dirname "$(realpath -s "${BASH_SOURCE[0]}")")
crdPath="$scriptDir/src/crds/eventhub.yaml"
specGroupReversed="org.zamariola.bruno.eventhubcontroller"
outputJavaPackage="org.zamariola.bruno.eventhubcontroller"
docker run \
  --rm \
  -v "$crdPath":"$crdPath" \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v "$(pwd)":"$(pwd)" \
  -ti \
  --network host \
  ghcr.io/kubernetes-client/java/crd-model-gen:v1.0.6 \
    /generate.sh \
    -u "$crdPath" \
    -n "$specGroupReversed" \
    -p "$outputJavaPackage" \
    -o "$(pwd)"
