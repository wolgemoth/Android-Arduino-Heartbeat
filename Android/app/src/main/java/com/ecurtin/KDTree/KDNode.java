package com.ecurtin.KDTree;

import java.util.ArrayList;

/**
 * The KDTree is constructed recursively by calling the constructor of the root node.
 * Each node contains a HyperRectangle, a Point[] (which may be null), and references to the right and left nodes (which may be null).
 * The root node is split along the mean of the 0th dimension of all the points into the right and left children. The children are then each
 * split along the mean of the 1st dimension, and so on down to the nth dimension where the split dimension rolls back over to 0, 1, etc.
 * Data (Point[]) are only stored in leaf nodes.
 * @author emily
 *
 */
public class KDNode 
{
	public Point[] pointArr;
	public HyperRectangle hyperRect;
	public KDNode rightNode;
	public KDNode leftNode;
	public int dimension;

	/**
	 * Calling the constructor for the root will build the KDTree. The KDTree class just holds
	 * a reference to the root and calls tree-wide functions.
	 * @param points
	 * @param splitDimension
	 */
	public KDNode(Point[] points, int splitDimension)
	{
		//System.out.println("building hyperRect with "+points.length+" points");
//		if(points.length < 9 )
//		{
//			for(Point p : points)
//			{
//				System.out.println(p);
//			}
//		}
		hyperRect = new HyperRectangle(points);
		rightNode = null;
		leftNode = null;

		//Ex: Points are 3d, splitDimension is 4, so roll it back to 0
		if(splitDimension >= points[0].size()) 
		{ 
			splitDimension = 0;
		}

		//this is really only necessary for testing
		dimension = splitDimension;

		//Only store points if there are 10 or less in the array
		if(points.length <= 10)
		{
			this.pointArr = points;
			return;
		}

		double dimensionMean = findMeanOfDimension(splitDimension, points);

		ArrayList<Point> rightList = new ArrayList<Point>();
		ArrayList<Point> leftList = new ArrayList<Point>();

		for(Point p : points)
		{
			if(p.get(splitDimension) <= dimensionMean)
			{
				leftList.add(p);
			}
			else
			{
				rightList.add(p);
			}
		}

		//Edge case. If one of these split sets ends up empty, don't split. Just call it a leaf
		if(rightList.size() == 0 || leftList.size() == 0)
		{
			this.pointArr = points;
			return;
		}

		//arrayList.toArray() was throwing classCastExceptions. This is a hack.
		Point[] right = new Point[rightList.size()];
		Point[] left = new Point[leftList.size()];
		for(int i = 0; i < rightList.size(); i++)
		{
			right[i] = rightList.get(i);
		}
		for(int i = 0; i < leftList.size(); i++)
		{
			left[i] = leftList.get(i);
		}

		//recursively build right and left nodes
		leftNode = new KDNode(left, splitDimension + 1);
		rightNode = new KDNode(right, splitDimension + 1);

	}

	/**
	 * Returns true if the node has children and false if it does not
	 * @return
	 */
	public boolean hasChildren()
	{
		if( this.leftNode != null || this.rightNode != null)
		{
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the node has a right child and false if it does not
	 * @return
	 */
	public boolean hasRight()
	{
		if( this.rightNode != null)
		{
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the node has a left child and false if it does not
	 * @return
	 */
	public boolean hasLeft()
	{
		if( this.leftNode != null)
		{
			return true;
		}
		return false;
	}

	/**
	 * Finds the mean of a certain dimension in a group of points
	 * @return
	 */
	public double findMeanOfDimension(int dimension, Point[] points)
	{
		double mean = 0;

		for(Point p : points)
		{
			mean += p.get(dimension);
		}

		mean = mean / points.length;
		return mean;
	}

	/**
	 * Returns a string representation of the bounds of that dimension
	 * @return
	 */
	public String boundsToString()
	{
		return "Dimension: "+dimension+"\n"+hyperRect.toString(dimension);
	}
}
