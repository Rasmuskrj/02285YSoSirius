package main.java.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by simon on 24/03/15.
 */
public class InputReader {
    private BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );

    private void readMap() throws IOException {
        Map< Character, String > colors = new HashMap< Character, String >();
        String line, color;

        // Read lines specifying colors
        while ( ( line = in.readLine() ).matches( "^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$" ) ) {
            line = line.replaceAll( "\\s", "" );
            color = line.split( ":" )[0];

            for ( String id : line.split( ":" )[1].split( "," ) )
                colors.put( id.charAt( 0 ), color );
        }

        // Read lines specifying level layout
        while ( !line.equals( "" ) ) {
            for ( int i = 0; i < line.length(); i++ ) {
                char id = line.charAt( i );
                if ( '0' <= id && id <= '9' )
                    agents.add( new Agent( id, colors.get( id ) ) );
            }

            line = in.readLine();

        }
    }
}
