# BigData Hadoop Homework
# Hadoop Mini-Cluster (Docker) — Java & Python MapReduce

This repository sets up a containerized **Hadoop mini-cluster** that supports both **Java MapReduce** and **Python Hadoop Streaming** jobs.

---

## 🧱 Project Structure

```
shared/
├── datasets/
│   └── gutenberg/              # Input text files
├── java_homework/
│   ├── src/LetterCount.java    # Java MapReduce program
│   ├── classes/                # Compiled .class files
│   └── lettercount.jar         # Packaged JAR
└── python_homework/
    ├── wordcount_map.py
    └── wordcount_reduce.py
```

---

## 🐳 Docker Image Setup

### Dockerfile
```dockerfile
FROM apache/hadoop:3
USER root

# Fix CentOS 7 vault mirrors and install Python, Java, utilities
RUN sed -i 's|mirrorlist=|#mirrorlist=|g' /etc/yum.repos.d/CentOS-*.repo &&     sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-*.repo &&     yum clean all && yum makecache &&     yum install -y epel-release &&     yum install -y python3 which java-1.8.0-openjdk-devel &&     ln -sf /usr/bin/python3 /usr/local/bin/python3 &&     yum clean all

ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk
ENV PATH=$JAVA_HOME/bin:$PATH

USER hadoop
```

---

## 🧩 Docker Compose Setup

### `docker-compose.yml`
```yaml
services:
  namenode:
    image: hadoop-custom:3
    container_name: namenode
    ulimits:
      nofile:
        soft: "65536"
        hard: "65536"
    hostname: namenode
    command: ["hdfs", "namenode"]
    volumes:
      - "${PWD}/shared:/mnt"
    ports:
      - 9870:9870
    env_file:
      - ./config
    environment:
      ENSURE_NAMENODE_DIR: "/tmp/hadoop-root/dfs/name"

  datanode:
    image: hadoop-custom:3
    container_name: datanode
    ulimits:
      nofile:
        soft: "65536"
        hard: "65536"
    command: ["hdfs", "datanode"]
    env_file:
      - ./config

  resourcemanager:
    image: hadoop-custom:3
    container_name: resourcemanager
    ulimits:
      nofile:
        soft: "65536"
        hard: "65536"
    hostname: resourcemanager
    command: ["yarn", "resourcemanager"]
    ports:
      - 8088:8088
    env_file:
      - ./config

  nodemanager:
    image: hadoop-custom:3
    container_name: nodemanager
    ulimits:
      nofile:
        soft: "65536"
        hard: "65536"
    command: ["yarn", "nodemanager"]
    env_file:
      - ./config
```

---

## 🚀 Steps to Build & Run

### 1. Build Image
```bash
docker build -t hadoop-custom:3 .
```

### 2. Start Cluster
```bash
docker compose up -d
```

---

## ⚙️ Inside NameNode (Run via Docker Exec)

You can use `docker exec` or open the container via Docker Desktop UI → Exec terminal.

> Checkout the files for list of all the commands to execute

1. [Java Commands](./java_commands.md)
2. [Python Commands](./python_commands.md)

### 1. Load Gutenberg Data
```bash
docker exec -it namenode bash -lc '
  hdfs dfs -mkdir -p /user/hadoop/input/gutenberg &&
  hdfs dfs -put -f /mnt/datasets/gutenberg/*.txt /user/hadoop/input/gutenberg/ &&
  hdfs dfs -ls -h /user/hadoop/input/gutenberg
'
```

---

### 2. Run Java MapReduce Job
```bash
docker exec -it namenode bash -lc '
  hdfs dfs -rm -r -f /user/hadoop/output/lettercount &&
  hadoop jar /mnt/java_homework/lettercount.jar LetterCount     /user/hadoop/input/gutenberg     /user/hadoop/output/lettercount &&
  hdfs dfs -cat /user/hadoop/output/lettercount/part-* | head -50
'
```

> 💡 If your Java class has a package declaration, use the fully qualified class name.

---

### 3. Run Python Hadoop Streaming Job
```bash
docker exec -it namenode bash -lc '
  chmod +x /mnt/python_homework/wordcount_map.py /mnt/python_homework/wordcount_reduce.py &&
  hdfs dfs -rm -r -f /user/hadoop/output/wc &&
  mapred streaming     -files /mnt/python_homework/wordcount_map.py,/mnt/python_homework/wordcount_reduce.py     -mapper wordcount_map.py     -reducer wordcount_reduce.py     -input /user/hadoop/input/gutenberg     -output /user/hadoop/output/wc &&
  hdfs dfs -cat /user/hadoop/output/wc/part-* | sort -k2 -nr | head -50
'
```

---

### 4. Recompile Java (Optional)
```bash
docker exec -it namenode bash -lc '
  cd /mnt/java_homework &&
  rm -rf classes && mkdir -p classes &&
  javac -cp "$(hadoop classpath)" -d classes src/LetterCount.java &&
  jar -cvf lettercount.jar -C classes/ . &&
  jar tf lettercount.jar | grep LetterCount
'
```

---

### 5. Tear Down
```bash
docker compose down
# or full reset:
docker compose down -v
```

---

## 🧭 UIs
- **NameNode:** http://localhost:9870  
- **ResourceManager:** http://localhost:8088  

---

## 🪄 Tip
You can open Docker Desktop → Containers → **namenode** → click **Exec**,  
and paste the same commands there instead of running `docker exec` manually.
