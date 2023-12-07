package com.ecurtin.KDTree;

import java.util.ArrayList;

/**
 * A point is an extension of the java arraylist class which also includes methods for 
 * finding the euclidian distance to another point, as well as custom toString and equals
 * @author Emily
 *
 */
public class Point extends ArrayList<Double> {

	public String m_Name    = null;
	public String m_IAPS_ID = null;

	private static final long serialVersionUID = 1L;

	/**
	 * Empty constructor
	 */
	public Point(String _name, String _IAPS_ID) {
		m_Name    = _name;
		m_IAPS_ID = _IAPS_ID;
	}

	/**
	 * Constructor takes in a double array
	 * @param arr
	 */
	public Point(double[] arr) {
		for(double d : arr)
		{
			this.add(d);
		}
	}


	/**
	 * Finds the Euclidean distance between two points of the same dimensionality
	 * Algorithm: Sqrt( SumOf( (x1i - x2i)^2 ) )
	 * @param otherPoint
	 * @return Euclidean distance between points
	 */
	public double distanceTo(Point otherPoint)
	{

		double insideSqrt = 0;

		for(int i = 0; i < this.size(); i++)
		{
			insideSqrt += Math.pow( (this.get(i) - otherPoint.get(i)), 2);
		}

		return Math.sqrt(insideSqrt);
	}

	/**
	 * Custom toString
	 * Ex: 4-D point containing 1, 2, 3, 4 would return "{1, 2, 3, 4,}"
	 */
	@Override
	public String toString()
	{
		String retString = "{";
		for( double d : this )
		{
			retString = retString+d+", ";
		}
		retString = retString+"}";
		return retString;
	}

	/**
	 * Does an O(n) comparison between each coordinate of the points
	 * @param otherPoint
	 * @return
	 */
	public boolean equals(Point otherPoint)
	{
		for(int i = 0; i < this.size(); i++)
		{
			if( this.get(i) != otherPoint.get(i))
			{
				return false;
			}
		}
		return true;
	}
}
