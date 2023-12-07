package com.ecurtin.KDTree;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Because the actual reading of the CSV was not an integral part of the project, I adapted a freely available implementation
 * from this website: http://www.mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
 * @author mkyong, adapted by Emily Curtin
 *
 */
public class CSVReader {


 public Point[] readFileReturnPoints(String filename) {

  BufferedReader br = null;
  String line = "";
  String cvsSplitBy = ",";
  ArrayList<Point> pointArrayList = new ArrayList<Point>();
  Point[] pointArr;

  try 
  {
   br = new BufferedReader(new FileReader(filename));
   while ((line = br.readLine()) != null) 
   {

    // use comma as separator
    String[] coordinatesStr = line.split(cvsSplitBy);
    double[] coordinates = new double[coordinatesStr.length];
    for(int i = 0; i < coordinatesStr.length; i++)
    {
     coordinates[i] = Double.parseDouble(coordinatesStr[i]);
    }
    //pointArrayList.add(new Point(coordinates));
   }

  } catch (FileNotFoundException e) 
  {
   e.printStackTrace();
  } catch (IOException e) 
  {
   e.printStackTrace();
  } finally 
  {
   if (br != null) {
    try {
     br.close();


    } catch (IOException e) {
     e.printStackTrace();
    }
   }
  }

  pointArr = new Point[pointArrayList.size()];
  for(int i = 0; i < pointArrayList.size(); i++)
  {
   pointArr[i] = pointArrayList.get(i);
  }

  return pointArr;
 }

 public int[] readFileReturnInts(String filename) {

  BufferedReader br = null;
  String line = "";
  String cvsSplitBy = ",";
  ArrayList<Integer> intArrayList = new ArrayList<Integer>();
  int[] intArr;

  try 
  {
   br = new BufferedReader(new FileReader(filename));
   while ((line = br.readLine()) != null) 
   {

    // use comma as separator
    String[] intStr = line.split(cvsSplitBy);
    for(int i = 0; i < intStr.length; i++)
    {
     intArrayList.add(Integer.parseInt(intStr[i]));
    }
   }

  } catch (FileNotFoundException e) 
  {
   e.printStackTrace();
  } catch (IOException e) 
  {
   e.printStackTrace();
  } finally 
  {
   if (br != null) {
    try {
     br.close();


    } catch (IOException e) {
     e.printStackTrace();
    }
   }
  }

  intArr = new int[intArrayList.size()];
  for(int i = 0; i < intArrayList.size(); i++)
  {
   intArr[i] = intArrayList.get(i);
  }

  return intArr;
 }
 
}