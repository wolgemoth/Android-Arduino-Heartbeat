package com.ecurtin.KDTree;

import java.util.ArrayList;

/**
 * The KDTree class holds a reference to the root node of a tree and provides a space for full-tree methods such as traversals
 * to be written. The tree itself is constructed recursively on the call to construct the root of the tree.
 * KDTrees are generally not implemented with dynamic add and delete methods because of the massive expense involved in recalculating
 * the bounds on each dimension. It's standard practice to build a KDTree from a large, static dataset, perhaps a matrix or a csv file
 * or something similar.
 * @author Emily Curtin
 *
 */
public class KDTree 
{
	KDNode root;
	Point[] pointArr;
	int dimensionality;

	/**
	 * Takes in a point Array and builds a KDTree
	 * @param pointArr
	 */
	public KDTree(Point[] pointArr)
	{
		this.pointArr = pointArr;
		this.dimensionality = pointArr[0].size();
		this.root = new KDNode(this.pointArr, 0);
	}

	/**
	 * Returns the closest point in the KDTree to the parameter point.
	 * @param searchPoint
	 * @return
	 */
	public Point nearestNeighborSearch(Point searchPoint)
	{
		double distanceToNeighbor = Double.MAX_VALUE;
		Point nearestNeighbor = null;

		nearestNeighbor = nnTraversal(root, searchPoint, nearestNeighbor, distanceToNeighbor);

		return nearestNeighbor;
	}

	/**
	 * Recursive method to traverse the tree to search through nodes of the tree and prune nodes as necessary.
	 * @param currentNode
	 * @param searchPoint
	 * @param nearestNeighbor
	 * @param distanceToNeighbor
	 * @return
	 */
	private Point nnTraversal(KDNode currentNode, Point searchPoint, Point nearestNeighbor, double distanceToNeighbor)
	{
		double distanceToCurrentNode = currentNode.hyperRect.minimumDistanceFromNearestRectangle(searchPoint);
		if(distanceToNeighbor > distanceToCurrentNode)
		{
			//Base Case: reach a leaf node and go through each data point to see if it is the closest.
			if( !currentNode.hasChildren())
			{
				for( Point p : currentNode.pointArr )
				{
					double distance = p.distanceTo(searchPoint);
					if( distance < distanceToNeighbor )
					{
						distanceToNeighbor = distance;
						nearestNeighbor = p;
					}
				}
			}
			else //recurse into the children
			{
				distanceToNeighbor = updateDistanceToNeighbor(searchPoint, nearestNeighbor);
				nearestNeighbor = nnTraversal(currentNode.leftNode, searchPoint, nearestNeighbor, distanceToNeighbor);
				distanceToNeighbor = updateDistanceToNeighbor(searchPoint, nearestNeighbor);
				nearestNeighbor = nnTraversal(currentNode.rightNode, searchPoint, nearestNeighbor, distanceToNeighbor);
			}
		}
		//  else { System.out.println("pruned!"); }
		//skip straight to here if distanceToNeighbor is less than the distance to the currentNode. You don't need to look down that branch.
		return nearestNeighbor;
	}

	/**
	 * Because the recursive method only returns a point, the distance to a neighbor needs to be updated
	 * manually.
	 * @param searchPoint
	 * @param nearestNeighbor
	 * @return
	 */
	private double updateDistanceToNeighbor(Point searchPoint, Point nearestNeighbor)
	{
		if (searchPoint == null || nearestNeighbor == null)
		{
			return Double.MAX_VALUE;
		}
		else
		{
			return searchPoint.distanceTo(nearestNeighbor);
		}
	}

	/////////////////////Data Structure Test Traversals////////////////////////

	/**
	 * Traverses the KDNode and adds each point to an arraylist. Used to test
	 * that all the points of the point array used to construct the tree are actually in the tree.
	 * @return
	 */
	public ArrayList<Point> inOrder() {
		ArrayList<Point> returnList = new ArrayList<Point>();
		inOrderTraverse(returnList, root);
		return returnList;
	}
/**
 * recursive method for above
 * @param fillList
 * @param parent
 */
	private void inOrderTraverse(ArrayList<Point> fillList, KDNode parent)
	{
		if( parent != null) 
		{
			this.inOrderTraverse(fillList, parent.leftNode);
			if( parent.pointArr != null )
			{
				System.out.println("Leaf Node "+parent.toString());  
				for(Point p : parent.pointArr)
				{
					fillList.add( p );
					System.out.println("     "+parent.toString()+": "+p.toString());
				}

			}
			else
			{
				System.out.println("Parent node");
			}
			this.inOrderTraverse(fillList, parent.rightNode);
		}
		return;
	}

	/**
	 * Traverses the tree to test that each of the nodes in the tree are contained
	 * within the hyperRectangle of the node.
	 */
	public void inOrderForPointContainmentCheck() 
	{
		inOrderTraverseForPointContainmentCheck(root);
	}
	
	/**
	 * Recursive helper method for above
	 * @param parent
	 */
	private void inOrderTraverseForPointContainmentCheck(KDNode parent)
	{
		if( parent != null) 
		{
			this.inOrderTraverseForPointContainmentCheck(parent.leftNode);
			if( parent.pointArr != null )
			{
				if( !parent.hyperRect.contains(parent.pointArr))
				{
					System.out.println("A point in node "+parent.toString()+" is not within the bounds");
				}
				else
				{
					System.out.println("All nodes within "+parent.toString()+" are within the bounds");
				}
			}
			else
			{
				System.out.println("Parent node");
			}
			this.inOrderTraverseForPointContainmentCheck(parent.rightNode);
		}
		return;
	}

	/**
	 * Traverses the tree to check that the right and left children of each parent node do not overlap.
	 */
	public void overlapCheck()
	{
		overlapCheckTraverse(root);
	}

	/**
	 * Recursive helper method for above.
	 * @param parent
	 */
	private void overlapCheckTraverse(KDNode parent) 
	{
		if(parent.leftNode == null || parent.rightNode == null)
		{
			return;
		}

		if(parent.leftNode.hyperRect.overlaps(parent.rightNode.hyperRect))
		{
			System.out.println("There is an overlap between nodes");
			return;
		}

		if(parent.leftNode != null)
		{ 
			overlapCheckTraverse(parent.leftNode);
		}
		if(parent.rightNode != null)
		{
			overlapCheckTraverse(parent.rightNode);
		}
		return;
	}


}
