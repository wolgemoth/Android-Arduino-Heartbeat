package com.ecurtin.KDTree;

/**
 * A simple class for holding two doubles.
 * @author Emily Curtin
 *
 */

public class MinMaxPair 
{
	double min;
	double max;

	public MinMaxPair()
	{
	}

	public MinMaxPair(double a, double b)
	{
		min = a;
		max = b;
	}

	public String toString()
	{
		return "Min: "+min+", Max: "+max;
	}

	public boolean equals(MinMaxPair otherPair)
	{
		if( this.min == otherPair.min && this.max == otherPair.max)
		{
			return true;
		}
		return false;
	}
}
