import java.awt.Color;
import java.util.function.Function;

import edu.princeton.cs.algs4.LinearProbingHashST;
import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.Stack;

public class SeamCarver
{
	private Picture pic;

	public SeamCarver(Picture picture) // create a seam carver object based on
										// the given picture
	{
		this.pic = picture;
	}

	public Picture picture() // current picture
	{
		return pic;
	}

	public int width() // width of current picture
	{
		return pic.width();
	}

	public int height() // height of current picture
	{
		return pic.height();
	}

	private double getDelta(Color c1, Color c2)
	{
		return Math.pow(c1.getRed() - c2.getRed(), 2) + Math.pow(c1.getBlue() - c2.getBlue(), 2)
				+ Math.pow(c1.getGreen() - c2.getGreen(), 2);
	}

	private boolean isInBound(int x, int y)
	{
		return x >= 0 && x < pic.width() && y >= 0 && y < pic.height();
	}

	private boolean isInBound(Point p)
	{
		return isInBound(p.x, p.y);
	}

	public double energy(int x, int y) // energy of pixel at column x and row y
	{
		if (!isInBound(x, y))
		{
			throw new IndexOutOfBoundsException();
		}
		if (x == 0 || x == width() - 1 || y == 0 || y == height() - 1)
		{
			return 1000.0D;
		}
		double dx = getDelta(pic.get(x - 1, y), pic.get(x + 1, y));
		double dy = getDelta(pic.get(x, y - 1), pic.get(x, y + 1));
		return Math.sqrt(dx + dy);
	}

	private Queue<Point> topologicalSort(Function<Point, Point[]> neighbors)
	{
		Queue<Point> topologicalOrder = new Queue<Point>();
		Stack<Point> stack = new Stack<Point>();
		LinearProbingHashSet<Point> visited = new LinearProbingHashSet<Point>();

		Point start = new Point(-1, -1);

		stack.push(start);
		outer: while (!stack.isEmpty())
		{
			Point cur = stack.peek();
			for (Point next : neighbors.apply(cur))
			{
				if (!visited.contains(next))
				{
					stack.push(next);
					visited.add(next);
					continue outer;
				}
			}
			topologicalOrder.enqueue(cur);
		}

		Point first = topologicalOrder.dequeue();
		assert first == start;

		return topologicalOrder;
	}

	private Queue<Point> horizontalTopSort()
	{
		return topologicalSort(p ->
		{
			if (p.x == -1 || p.y == -1)
			{
				assert p.x == -1 && p.y == -1;

				Point[] neighbors = new Point[pic.height()];
				for (int i = 0; i < neighbors.length; i++)
					neighbors[i] = new Point(0, i);

				return neighbors;
			} else
			{
				if (p.x >= pic.width() - 1)
				{
					return new Point[0];
				}
				Queue<Point> q = new Queue<Point>();
				for (int offset = -1; offset <= 1; offset++)
				{
					Point next = new Point(p.x + 1, p.y + offset);
					if (isInBound(next))
						q.enqueue(next);
				}

				Point[] neighbors = new Point[q.size()];

				for (int i = 0; i < neighbors.length; i++)
					neighbors[i] = q.dequeue();

				assert neighbors.length <= 3;
				return neighbors;
			}
		});
	}

	private Queue<Point> verticalTopSort()
	{
		return topologicalSort(p ->
		{
			if (p.x == -1 || p.y == -1)
			{
				assert p.x == -1 && p.y == -1;

				Point[] neighbors = new Point[pic.width()];
				for (int i = 0; i < neighbors.length; i++)
					neighbors[i] = new Point(i, 0);

				return neighbors;
			} else
			{
				if (p.x >= pic.height() - 1)
				{
					return new Point[0];
				}
				Queue<Point> q = new Queue<Point>();
				for (int offset = -1; offset <= 1; offset++)
				{
					Point next = new Point(p.x + offset, p.y + 1);
					if (isInBound(next))
						q.enqueue(next);
				}

				Point[] neighbors = new Point[q.size()];

				for (int i = 0; i < neighbors.length; i++)
					neighbors[i] = q.dequeue();

				assert neighbors.length <= 3;
				return neighbors;
			}
		});
	}

	private void relax(Point cur, Point next, LinearProbingHashST<Point, Double> dist,
			LinearProbingHashST<Point, Point> from)
	{
		double curCost = dist.get(cur);
		Double oldCost = dist.get(next);
		double edgeWeight = energy(next.x, next.y);
		if (oldCost == null || curCost + edgeWeight < oldCost)
		{
			dist.put(next, curCost + edgeWeight);
			from.put(next, cur);
		}
	}

	// private int[] findSeam(Iterable<Point> topologicalOrder,
	// LinearProbingHashST<Point, Double> dist,
	// LinearProbingHashST<Point, Point> from)
	// {
	// int[] seam = new
	// return null;
	// }

	public int[] findHorizontalSeam() // sequence of indices for horizontal seam
	{
		Queue<Point> topologicalOrder = horizontalTopSort();
		LinearProbingHashST<Point, Double> dist = new LinearProbingHashST<Point, Double>();
		LinearProbingHashST<Point, Point> from = new LinearProbingHashST<Point, Point>();

		Point start = new Point(-1, -1);
		Point end = new Point(pic.width(), pic.height());

		for (int i = 0; i < pic.height(); i++)
		{
			Point p = new Point(0, i);
			from.put(p, start);
			dist.put(p, energy(p.x, p.y));
		}

		while (!topologicalOrder.isEmpty())
		{
			Point cur = topologicalOrder.dequeue();
			Point a = new Point(cur.x + 1, cur.y + 1);
			Point b = new Point(cur.x + 1, cur.y);
			Point c = new Point(cur.x + 1, cur.y - 1);
			if (isInBound(a))
			{
				relax(cur, a, dist, from);
			}
			if (isInBound(b))
			{
				relax(cur, b, dist, from);
			}
			if (isInBound(c))
			{
				relax(cur, c, dist, from);
			}
			if (cur.x + 1 >= pic.width())
			{
				double curCost = dist.get(cur);
				Double oldCost = dist.get(end);
				if (oldCost == null || curCost < oldCost)
				{
					dist.put(end, curCost);
					from.put(end, cur);
				}
			}
		}

		int[] seam = new int[pic.width()];
		Point cur = end;
		for (int i = pic.width() - 1; i >= 0; i--)
		{
			Point prev = from.get(cur);
			seam[i] = prev.y;
			cur = prev;
		}
		return seam;
	}

	public int[] findVerticalSeam() // sequence of indices for vertical seam
	{
		Queue<Point> topologicalOrder = verticalTopSort();
		LinearProbingHashST<Point, Double> dist = new LinearProbingHashST<Point, Double>();
		LinearProbingHashST<Point, Point> from = new LinearProbingHashST<Point, Point>();

		Point start = new Point(-1, -1);
		Point end = new Point(pic.width(), pic.height());

		for (int i = 0; i < pic.height(); i++)
		{
			Point p = new Point(0, i);
			from.put(p, start);
			dist.put(p, energy(p.x, p.y));
		}

		while (!topologicalOrder.isEmpty())
		{
			Point cur = topologicalOrder.dequeue();
			Point a = new Point(cur.x + 1, cur.y + 1);
			Point b = new Point(cur.x + 1, cur.y);
			Point c = new Point(cur.x + 1, cur.y - 1);
			if (isInBound(a))
			{
				relax(cur, a, dist, from);
			}
			if (isInBound(b))
			{
				relax(cur, b, dist, from);
			}
			if (isInBound(c))
			{
				relax(cur, c, dist, from);
			}
			if (cur.x + 1 >= pic.width())
			{
				double curCost = dist.get(cur);
				Double oldCost = dist.get(end);
				if (oldCost == null || curCost < oldCost)
				{
					dist.put(end, curCost);
					from.put(end, cur);
				}
			}
		}

		int[] seam = new int[pic.width()];
		Point cur = end;
		for (int i = pic.width() - 1; i >= 0; i--)
		{
			Point prev = from.get(cur);
			seam[i] = prev.y;
			cur = prev;
		}
		return seam;
	}

	public void removeHorizontalSeam(int[] seam) // remove horizontal seam from
													// current picture
	{
	}

	public void removeVerticalSeam(int[] seam) // remove vertical seam from
												// current picture
	{
	}
}