# Load Gutenberg into HDFS
```bash
hdfs dfs -mkdir -p /user/hadoop/input/gutenberg
hdfs dfs -put -f /mnt/datasets/gutenberg/*.txt /user/hadoop/input/gutenberg/
hdfs dfs -ls -h /user/hadoop/input/gutenberg
```

# clean old output
```bash
hdfs dfs -rm -r -f /user/hadoop/output/lettercount
```

# run jar (main class at jar root is 'LetterCount')
```bash
hadoop jar /mnt/java_homework/lettercount.jar LetterCount \
  /user/hadoop/input/gutenberg \
  /user/hadoop/output/lettercount
```

# read results
```bash
hdfs dfs -cat /user/hadoop/output/lettercount/part-* | head -50
```
