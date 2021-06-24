package tools.statplotter.parser;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class StatFileParser implements FileParser {

    private static String TABLE_SEPARATOR = "------------------------------------------------------------------------------------";

    private static String FIELD_SEPARATOR = "\\|";

    private final InputStreamReader stream;
    private final BufferedReader bufferedStream;

    public StatFileParser(InputStream inputstream){
        stream = new InputStreamReader(inputstream);
        this.bufferedStream = new BufferedReader(stream);
    }

    private boolean isSeparator(String line) throws IOException{
        return line.equals(TABLE_SEPARATOR);
    }

    private Date getDate(String dateLine) throws ParseException {
        Pattern regex = Pattern.compile("Date: (\\d{1,2}\\/\\d{1,2}\\/\\d{4} -- \\d{2}:\\d{2}:\\d{2})", Pattern.CASE_INSENSITIVE);
        Matcher match;
        match = regex.matcher(dateLine);
        if (match.find()){
            //Fecha. El fichero stats por defecto pone la fecha local del sistema siempre.
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy -- HH:mm:ss");
            return simpleDateFormat.parse(match.group(1));
        }
        return null;
    }

    private long getUptime(String dateLine) {
        int count;
        Pattern regex = Pattern.compile("uptime: (\\d+)d, (\\d+)h (\\d+)m (\\d+)s", Pattern.CASE_INSENSITIVE);
        Matcher match;
        match = regex.matcher(dateLine);
        if (match.find()){
            //Uptime
            count = Integer.parseInt(match.group(4));
            count += Integer.parseInt(match.group(3)) * 60;
            count += Integer.parseInt(match.group(2)) * 3600;
            count += Integer.parseInt(match.group(1)) * 86400;
            return count;
        }
        return 0;
    }

    private String getStat(String tableLine){
        String[] fields = tableLine.split(FIELD_SEPARATOR);
        return fields[0].trim();
    }

    private String getSource(String tableLine){
        String[] fields = tableLine.split(FIELD_SEPARATOR);
        return fields[1].trim();
    }

    private Long getValue(String tableLine){
        String[] fields = tableLine.split(FIELD_SEPARATOR);
        return Long.parseLong(fields[2].trim());
    }

    @Override
    public Collection<StatTimeSerie> readStats() throws IOException, ParseException {
        Map<String, StatTimeSerie> stats = new HashMap<>();
        Time time;
        StatTimeSerie serie;
        String statSource, statName;
        long statValue;
        int line = 0;
        if (bufferedStream.ready()) {
            String currentLine = bufferedStream.readLine(); // --------------------------------------
            line++;
            while (currentLine != null) {

                if (!isSeparator(currentLine)) {
                    throw new IOException("Table start expected, found: " + currentLine);
                }
                // Table start
                if (line == 249){
                    System.err.println(line);
                }
                currentLine = bufferedStream.readLine(); // Date header
                line++;
                time = new Time(getDate(currentLine), getUptime(currentLine));

                if (time.getDate() == null){
                    throw new IOException("Date not parsed on line " + line + ": " + currentLine);
                }
                if (time.getUptime() == 0){
                    System.err.println("Warning: suspicious 0 uptime value on line " + line + ": " + currentLine);
                }

                bufferedStream.readLine(); // --------------------------------------
                bufferedStream.readLine(); // Table header
                bufferedStream.readLine(); // --------------------------------------
                line+=3;
                currentLine = bufferedStream.readLine(); // First row
                line++;
                while (currentLine != null && !isSeparator(currentLine)) {
                    statSource = getSource(currentLine);
                    if (statSource.isEmpty()){
                        throw new IOException("Stat source not parsed in line " + line + ": " + currentLine);
                    }
                    statSource = statSource.concat(": ");
                    statName = getStat(currentLine);
                    if (statSource.isEmpty()){
                        throw new IOException("Stat name not parsed in line " + line + ": " + currentLine);
                    }
                    statName = statSource.concat(statName);
                    statValue = getValue(currentLine);
                    if (stats.containsKey(statName)) {
                        serie = stats.get(statName);
                        serie.addValue(time, statValue);
                    } else {
                        serie = new StatTimeSerie(statName);
                        serie.addValue(time, statValue);
                        stats.put(statName, serie);
                    }
                    currentLine = bufferedStream.readLine(); // Table row
                    line++;
                }
                // --------------------------------------
                // Table end
            }
        }
        return stats.values();
    }

    @Override
    public void reset() throws IOException {
        bufferedStream.reset();
        stream.reset();
    }

    @Override
    public void close() throws IOException {
        bufferedStream.close();
        if (stream != null){
            stream.close();
        }
    }
}
