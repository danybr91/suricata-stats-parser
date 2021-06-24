package tools.statplotter;

import org.junit.Test;
import tools.statplotter.parser.StatTimeSerie;
import tools.statplotter.parser.FileParser;
import tools.statplotter.parser.FileParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Collection;

public class StatFileParserTest {

    @Test
    public void ReadFileTest(){
        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream("stats.log")){
            FileParser parser = FileParserFactory.getParser(inputStream, "log");
            Collection<StatTimeSerie> stats = parser.readStats();
            assert(stats.size() == 27);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
