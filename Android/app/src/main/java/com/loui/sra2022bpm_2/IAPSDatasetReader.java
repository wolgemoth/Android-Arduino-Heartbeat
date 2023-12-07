package com.loui.sra2022bpm_2;

import android.content.Context;

import com.ecurtin.KDTree.Point;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IAPSDatasetReader {

    public static Point[] Read(Context _context, String _file) throws IOException, NumberFormatException {

        ArrayList<Point> points = new ArrayList<Point>();

            InputStream input = _context.getAssets().open(_file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        Pattern numberPattern = Pattern.compile("[0-9]+\\.[0-9]+");

        String line;

        while ((line = reader.readLine()) != null) {

            if (line.isEmpty() == false) {

                String[] subStrings = line.split(",");

                String name    = subStrings[0];
                String IAPS_ID = subStrings[1];

                Point point = new Point(name, IAPS_ID);

                double[] data = new double[8];

                int index = 0;

                for (int i = 2; i < subStrings.length; i++) {

                    Matcher numberMatcher = numberPattern.matcher(subStrings[i]);
                    if (numberMatcher.find()) {
                        data[index] = Double.parseDouble(numberMatcher.group().trim());
                    }

                    if (++index == data.length) { break; }
                }

                for (int i = 0; i < data.length; i++) {
                    point.add(data[i]);
                }

                points.add(point);
            }
        }

        Point[] result = new Point[points.size()];
        points.toArray(result);

        return result;
    }
}
