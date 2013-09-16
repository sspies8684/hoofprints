package net.decix.rs.tc;

import java.awt.Rectangle;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

import de.progra.charting.ChartEncoder;
import de.progra.charting.DefaultChart;
import de.progra.charting.model.DefaultChartDataModel;
import de.progra.charting.render.LineChartRenderer;

public class RSTCGrapher
{
	private List<Double> columns = new LinkedList<Double>();
	private List<Double> values = new LinkedList<Double>();
	private int width = 800;
	private int height = 600;
	private String[] rows;
	private double[][] model;
	private DefaultChartDataModel data;
	private DefaultChart chart;
	private String title;
	private long startTime = 0;

	public RSTCGrapher(String title, String key)
	{
		this.rows = new String[] { key };
		this.title = title;
	}

	public void addValue(double key, double value)
	{
		if (startTime == 0)
		{
			startTime = System.currentTimeMillis();
			columns.add(0.0);
			// bug workaround
			values.add(value + 0.1);
		}
		else
		{
			columns.add(key - startTime);
			values.add(value);
		}
	}

	public void createGraph()
	{

		double[] keys = new double[columns.size()];
		int i = 0;
		for (double c : columns)
			keys[i++] = c;

		model = new double[1][values.size()];
		i = 0;
		for (double v : values)
			model[0][i++] = v;

		data = new DefaultChartDataModel(model, keys, rows);

		chart = new DefaultChart(data, title, DefaultChart.LINEAR_X_LINEAR_Y);

		chart.addChartRenderer(new LineChartRenderer(chart.getCoordSystem(), data), 1);
		chart.setBounds(new Rectangle(0, 0, width, height));

		try
		{
			ChartEncoder.createEncodedImage(new FileOutputStream("tmp/" + (title + "_" + rows[0] + "_" + System.currentTimeMillis() + ".png").replaceAll("\\/", "")), chart, "png");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
	
	
}
