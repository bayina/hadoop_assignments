# Ensure executables
```bash
chmod +x /mnt/python_homework/wordcount_map.py /mnt/python_homework/wordcount_reduce.py
command -v dos2unix >/dev/null 2>&1 && dos2unix /mnt/python_homework/wordcount_map.py /mnt/python_homework/wordcount_reduce.py
```
# Load Gutenberg into HDFS
```bash
hdfs dfs -mkdir -p /user/hadoop/input/gutenberg
hdfs dfs -put -f /mnt/datasets/gutenberg/*.txt /user/hadoop/input/gutenberg/
hdfs dfs -ls -h /user/hadoop/input/gutenberg
```

# clean old output
```bash
hdfs dfs -rm -r -f /user/hadoop/output/wc
```

# run streaming (ship files via -files; call by basename)
```bash
mapred streaming \
  -files /mnt/python_homework/wordcount_map.py,/mnt/python_homework/wordcount_reduce.py \
  -mapper wordcount_map.py \
  -reducer wordcount_reduce.py \
  -input /user/hadoop/input/gutenberg \
  -output /user/hadoop/output/wc
```

# read results
```bash
hdfs dfs -cat /user/hadoop/output/wc/part-* | sort -k2 -nr
```

# read results (top 50 by count)
```bash
hdfs dfs -cat /user/hadoop/output/wc/part-* | sort -k2 -nr | head -50
```
