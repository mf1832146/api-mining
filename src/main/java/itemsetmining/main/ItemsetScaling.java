package itemsetmining.main;

import itemsetmining.itemset.Itemset;
import itemsetmining.main.InferenceAlgorithms.InferGreedy;
import itemsetmining.main.InferenceAlgorithms.InferenceAlgorithm;
import itemsetmining.transaction.TransactionGenerator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.math.plot.Plot2DPanel;

public class ItemsetScaling {

	private static final String name = "overlap";
	private static final File dbFile = new File("/tmp/itemset.txt");
	private static final File plotDir = new File(
			"/afs/inf.ed.ac.uk/user/j/jfowkes");
	private static final InferenceAlgorithm inferenceAlg = new InferGreedy();

	private static final int noSamples = 10;
	private static final int difficultyLevel = 0;

	private static final int noLogTransactions = 5;
	private static final int noExtraSets = 5;
	private static final int maxSetSize = 3;

	private static final int maxStructureSteps = 500;
	private static final int maxEMIterations = 20;

	public static void main(final String[] args) throws IOException {

		final double[] trans = new double[noLogTransactions];
		final double[] time = new double[noLogTransactions];

		// Generate real itemsets
		final HashMap<Itemset, Double> actualItemsets = TransactionGenerator
				.generateItemsets(name, difficultyLevel, noExtraSets,
						maxSetSize);
		System.out.print("\n============= ACTUAL ITEMSETS =============\n");
		for (final Entry<Itemset, Double> entry : actualItemsets.entrySet()) {
			System.out.print(String.format("%s\tprob: %1.5f %n",
					entry.getKey(), entry.getValue()));
		}
		System.out.print("\n");

		for (int i = 0; i < noLogTransactions; i++) {

			final int tran = (int) Math.pow(10, i + 1);
			System.out.println("\n========= 10^" + (i + 1) + " Transactions");

			// Generate transaction database
			TransactionGenerator.generateTransactionDatabase(actualItemsets,
					tran, dbFile);

			for (int sample = 0; sample < noSamples; sample++) {
				System.out.println("\n========= Sample " + (sample + 1)
						+ " of " + noSamples);

				// Mine itemsets
				final long startTime = System.currentTimeMillis();
				final HashMap<Itemset, Double> minedItemsets = ItemsetMining
						.mineItemsets(dbFile, inferenceAlg, maxStructureSteps,
								maxEMIterations);
				final long endTime = System.currentTimeMillis();
				final double tim = (endTime - startTime) / (double) 1000;
				time[i] += tim;

				System.out.printf("Time (s): %.2f%n", tim);
			}
		}

		for (int i = 0; i < noLogTransactions; i++) {

			// Average over samples
			time[i] /= noSamples;
			trans[i] = (int) Math.pow(10, i + 1);

			// Display average precision and recall
			System.out.println("\n========= No Transactions: " + trans[i]);
			System.out.printf("Average Time (s): %.2f%n", time[i]);
		}

		double avgAvgTime = 0;
		for (int i = 0; i < noLogTransactions; i++)
			avgAvgTime += time[i];
		avgAvgTime /= noLogTransactions;
		System.out.printf("\nAverage Average Time (s): %.2f%n", avgAvgTime);

		// Plot time
		final Plot2DPanel plot = new Plot2DPanel();
		plot.addLinePlot("", Color.blue, trans, time);
		plot.setAxisScale(0, "log");
		plot.setAxisLabels("No. Transactions", "Time (s)");

		// Display
		final JFrame frame = new JFrame("Results");
		frame.setSize(800, 800);
		frame.setContentPane(plot);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Save
		final BufferedImage image = new BufferedImage(frame.getWidth(),
				frame.getHeight(), BufferedImage.TYPE_INT_RGB);
		final Graphics2D graphics2D = image.createGraphics();
		frame.paint(graphics2D);
		ImageIO.write(image, "jpeg", new File(plotDir + "/" + name
				+ "_time.jpg"));

	}
}
