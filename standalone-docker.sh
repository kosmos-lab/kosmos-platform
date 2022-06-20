#! /bin/bash -

kosmos_standalone_target="${kosmos_standalone_target:="_standalone"}"
echo "target is $kosmos_standalone_target"
mvnv=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
mvn clean install -DskipTests=false
mkdir "$kosmos_standalone_target"
mkdir -p "$kosmos_standalone_target/ha-config/custom_components/kosmos"
mkdir -p "$kosmos_standalone_target/config"
mkdir -p "$kosmos_standalone_target/oz"
rsync -avzh web/oz web/kree web/js web/css web/index.html web/live.html web/doc web/gesture  "$kosmos_standalone_target/web"
rsync -avzh schema "$kosmos_standalone_target"
rsync -avzh rules/requirements.txt rules/kosmos.py "$kosmos_standalone_target/rules"
rsync -avzh target/lib target/kosmos.jar "$kosmos_standalone_target/target"
cp standalone-config.json "$kosmos_standalone_target/config/config.json"
cp standalone-Dockerfile "$kosmos_standalone_target/Dockerfile"
cp web/oz/ui.json "$kosmos_standalone_target/oz"
cp standalone-startup.sh "$kosmos_standalone_target/startup.sh"
cp standalone-docker-compose.yaml "$kosmos_standalone_target/docker-compose.yaml"
cp docker/ha/config/custom_components/kosmos/*.py "$kosmos_standalone_target/ha-config/custom_components/kosmos/"
cp docker/ha/config/custom_components/kosmos/*.json "$kosmos_standalone_target/ha-config/custom_components/kosmos/"
wget https://raw.githubusercontent.com/tribut/homeassistant-docker-venv/master/run -O "$kosmos_standalone_target/ha-config/run"
test -f "$kosmos_standalone_target/oz/ui.json" || echo "[]" > "$kosmos_standalone_target/oz/ui.json"
cd "$kosmos_standalone_target"
docker build -t ghcr.io/kosmos-lab/kosmos-platform:latest .
docker tag ghcr.io/kosmos-lab/kosmos-platform:latest "ghcr.io/kosmos-lab/kosmos-platform:$mvnv"
rm -rf rules/*
docker push ghcr.io/kosmos-lab/kosmos-platform:latest
docker push "ghcr.io/kosmos-lab/kosmos-platform:$mvnv"