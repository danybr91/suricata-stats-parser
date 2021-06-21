package tools.statplotter;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Collection;

public class StatFileParserTest {

    @Test
    public void ReadFileTest(){
        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream("stats.log")){
            StatFileParser parser = new StatFileParser(inputStream);
            Collection<StatTimeSerie> stats = parser.readStats();
            assert(stats.size() == 27);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
