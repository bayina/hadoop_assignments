/*
 * Name: Chandhini Bayina
 * Number: 700756775
 */

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class LetterCount extends Configured implements Tool {

    // Mapper: for each alphabetic character, emit (UPPERCASE_LETTER, 1) and ("total", 1)
    public static class LetterMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
        private static final IntWritable ONE = new IntWritable(1);
        private final Text outKey = new Text();
        private static final Text TOTAL_KEY = new Text("total");

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            char[] chars = value.toString().toCharArray();
            for (char c : chars) {
                if (Character.isLetter(c)) {
                    char up = Character.toUpperCase(c);
                    // Only count A-Z; skip letters outside ASCII A-Z if present
                    if (up >= 'A' && up <= 'Z') {
                        outKey.set(String.valueOf(up));
                        context.write(outKey, ONE);
                        context.write(TOTAL_KEY, ONE);
                    }
                }
            }
        }
    }

    // Combiner: sums counts locally to reduce shuffle
    public static class SumCombiner extends Reducer<Text, IntWritable, Text, IntWritable> {
        private final IntWritable outVal = new IntWritable();

        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable v : values) sum += v.get();
            outVal.set(sum);
            context.write(key, outVal);
        }
    }

    // Reducer: sums counts; outputs (letter, count) and ("total", grand_total)
    public static class SumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        private final IntWritable outVal = new IntWritable();

        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable v : values) sum += v.get();
            outVal.set(sum);
            context.write(key, outVal);
        }
    }

    @Override
    public int run(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: LetterCount <input> <output>");
            return 1;
        }
        Configuration conf = getConf();
        Job job = Job.getInstance(conf, "LetterCount");
        job.setJarByClass(LetterCount.class);

        job.setMapperClass(LetterMapper.class);
        job.setCombinerClass(SumCombiner.class);
        job.setReducerClass(SumReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // Single reducer for one part file
        job.setNumReduceTasks(1);

        FileInputFormat.setInputDirRecursive(job, true);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        return job.waitForCompletion(true) ? 0 : 2;
    }

    public static void main(String[] args) throws Exception {
        int ec = ToolRunner.run(new Configuration(), new LetterCount(), args);
        System.exit(ec);
    }
}
