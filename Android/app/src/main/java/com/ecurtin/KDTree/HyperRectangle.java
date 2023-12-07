package com.ecurtin.KDTree;

import java.util.Arrays;

/**
 * At it's core, a HyperRectangle is an array of MinMaxPairs. Each pair represents a bound of the rectangle on a certain dimension.
 * The HyperRectangle bound is put in its own class to account for the possibility of using different spatial bounds such as circles when
 * constructing KDTrees.
 * @author Emily Curtin
 *
 */
public class HyperRectangle 
{
	MinMaxPair[] minMaxArr;

	/**
	 * Takes in a pointArray, initializes the min-max pairs to Double.MAX and Double.MIN respectively,
	 * then calls findMinsAndMaxes to determine the actual boundaries for each dimension.
	 * @param pointArr
	 */
	public HyperRectangle(Point[] pointArr)
	{
		minMaxArr = new MinMaxPair[pointArr[0].size()];

		//  System.out.println("pointArr[0].size = "+pointArr[0].size());
		for(int i = 0; i < minMaxArr.length; i++)
		{
			minMaxArr[i] = new MinMaxPair(Double.MAX_VALUE, Double.MIN_VALUE);
		}

		findMinsAndMaxes(pointArr);
	}

	/**
	 * Finds the minimum and maximum points and sets the boundaries for that dimension
	 * to those values.
	 * @param pointArr
	 */
	private void findMinsAndMaxes(Point[] pointArr)
	{
		//  System.out.println("Size of minMaxArr = "+minMaxArr.length);
		//  System.out.println("Size of pointArr = "+pointArr.length);
		//  System.out.println("pointArr[0].get(0) = "+pointArr[0].get(0));


		for(int i = 0; i < minMaxArr.length; i++)
		{
			//   System.out.println("i = "+i);
			double temp = pointArr[0].get(i);
			//   System.out.println("temp = "+temp);
			minMaxArr[i].min = temp;
			minMaxArr[i].max = temp;

			for(int j = 0; j < pointArr.length; j++ )
			{
				if( pointArr[j].get(i) < minMaxArr[i].min)
				{
					minMaxArr[i].min = pointArr[j].get(i);
				}
				else if( pointArr[j].get(i) > minMaxArr[i].max)
				{
					minMaxArr[i].max = pointArr[j].get(i);
				}
			}
		}
	}

	/**
	 * Getter for the maximum boundary in the ith dimension
	 * @param i
	 * @return
	 */
	public double getMaxOfDimension(int i)
	{
		return minMaxArr[i].max;
	}

	/**
	 * Getter for the minimum boundary in the ith dimension
	 * @param i
	 * @return
	 */
	public double getMinOfDimension(int i)
	{
		return minMaxArr[i].min;
	}

	/**
	 * Calculates the distance between a point and this hyperRectangle. Accounts for 
	 * points within, right on, and outside of the boundaries of the rectangle.
	 * @param point
	 * @return
	 */
	public double minimumDistanceFromNearestRectangle(Point point)
	{
		double dmin = 0;
		double insideSqrt = 0;

		for(int i = 0; i < minMaxArr.length; i++)
		{
			insideSqrt += Math.pow(dmin(point.get(i), minMaxArr[i]), 2);
		}

		dmin = Math.sqrt(insideSqrt);
		return dmin;
	}

	/**
	 * Sqrt(sum of(linear distance between a coordinate in a certain dimension
	 * and the boundaries of that dimension
	 * @param dimension
	 * @param pair
	 * @return
	 */
	private double dmin(double dimension, MinMaxPair pair) 
	{
		//On the boundary counts as within the boundary
		if( pair.min <= dimension && dimension <= pair.max)
		{
			return 0;
		}
		else if( dimension < pair.min)
		{
			return (pair.min - dimension);
		}
		else if( dimension > pair.max)
		{
			return (dimension - pair.max);
		}
		//shouldn't get here
		return Double.NaN;
	}

	/**
	 * Determines whether a given point is within the hyperrectangle bounds
	 * @param pointArr
	 * @return
	 */
	public boolean contains(Point[] pointArr)
	{
		for(int i = 0; i < pointArr[0].size(); i++ )
		{
			for(Point p : pointArr)
			{
				if( minMaxArr[i].min <= p.get(i) && p.get(i) <= minMaxArr[i].max)
				{
					continue;
				}
				else
				{
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * toString returns the array toString of the minMaxArr
	 */
	public String toString()
	{
		return Arrays.toString(minMaxArr);
	}

	/**
	 * Returns the minMaxPair toString of the given dimension
	 * @param dimension
	 * @return
	 */
	public String toString(int dimension)
	{
		return minMaxArr[dimension].toString();
	}

	/**
	 * Determines if two hyperRectangels overlap
	 * @param hyperRect
	 * @return
	 */
	public boolean overlaps(HyperRectangle hyperRect) 
	{
		//lesser denotes "closer to negative infinity"
		HyperRectangle lesser;
		HyperRectangle greater;
		if( this.minMaxArr[0].min < hyperRect.minMaxArr[0].min)
		{
			lesser = this;
			greater = hyperRect;
		}
		else
		{
			lesser = hyperRect;
			greater = this;
		}

		for(int i = 0; i < this.minMaxArr.length; i++)
		{
			if( lesser.minMaxArr[i].max < greater.minMaxArr[i].min) 
			{
				return false;
			}
		}

		return true;
	}
}
