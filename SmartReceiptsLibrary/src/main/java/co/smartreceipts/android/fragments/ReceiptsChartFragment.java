package co.smartreceipts.android.fragments;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.DatabaseHelper;

public class ReceiptsChartFragment extends ReceiptsFragment implements DatabaseHelper.ReceiptRowGraphListener {

	public static final String TAG = "ReceiptsChartFragment";

	private LinearLayout mChartsLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onCreateView");
		}
		View rootView = inflater.inflate(R.layout.receipt_chart_list, container, false);
		mChartsLayout = (LinearLayout) rootView.findViewById(R.id.receipt_chart_list);
		TextView tv1 = new TextView(getActivity());
		TextView tv2 = new TextView(getActivity());
		tv1.setText("Start");
		tv2.setText("End");
		mChartsLayout.addView(tv1);
		mChartsLayout.addView(getTestView(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
		mChartsLayout.addView(tv2);
		/*
		Intent intent = getBarChart();
		getActivity().startActivity(intent);
		*/
		return rootView;
	}

	@Override
	public void onResume() {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onResume");
		}
		super.onResume();
		getPersistenceManager().getDatabase().registerReceiptRowGraphListener(this);
		getPersistenceManager().getDatabase().getCostPerCategoryParallel(mCurrentTrip);
	}

	@Override
	public void onPause() {
		getPersistenceManager().getDatabase().unregisterReceiptRowGraphListener();
		super.onPause();
	}

	private GraphicalView getTestView() {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

        XYSeries ac = new XYSeries("AC");
        XYSeries pe = new XYSeries("PE");
        XYSeries wa = new XYSeries("WA");
        XYSeries tl = new XYSeries("TL");
        XYSeries ml = new XYSeries("ML");
        XYSeries ce = new XYSeries("CE");
        XYSeries re = new XYSeries("RE");
        XYSeries ot = new XYSeries("OT");
        ac.add(1, 291);
        pe.add(2, 11);
        wa.add(3, 204);
        tl.add(4, 28);
        ml.add(5, 0);
        ce.add(6, 21);
        re.add(7, 51);
        ot.add(8, 4);
        dataset.addSeries(ac);
        dataset.addSeries(pe);
        dataset.addSeries(wa);
        dataset.addSeries(tl);
        dataset.addSeries(ml);
        dataset.addSeries(ce);
        dataset.addSeries(re);
        dataset.addSeries(ot);


        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.setChartTitle("Submission Statistics");
        mRenderer.setXTitle("Verdict Code");
        mRenderer.setYTitle("No. of Submissions");
        mRenderer.setAxesColor(Color.BLACK);
        mRenderer.setLabelsColor(Color.BLACK);
        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setBackgroundColor(Color.WHITE);
        mRenderer.setMarginsColor(Color.WHITE);
        mRenderer.setZoomEnabled(true);
        mRenderer.setZoomButtonsVisible(true);
        mRenderer.setBarSpacing(-0.5);
        mRenderer.setAxisTitleTextSize(16);
        mRenderer.setChartTitleTextSize(20);
        mRenderer.setLabelsTextSize(15);
        mRenderer.setLegendTextSize(15);
        mRenderer.addXTextLabel(1, "AC");
        mRenderer.addXTextLabel(2, "PE");
        mRenderer.addXTextLabel(3, "WA");
        mRenderer.addXTextLabel(4, "TL");
        mRenderer.addXTextLabel(5, "ML");
        mRenderer.addXTextLabel(6, "CE");
        mRenderer.addXTextLabel(7, "RE");
        mRenderer.addXTextLabel(8, "OT");
        mRenderer.setBarWidth(50);
        mRenderer.setXAxisMin(0);
        mRenderer.setYAxisMin(0);

        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setColor(Color.parseColor("#00AA00"));
        renderer.setDisplayChartValues(true);

        XYSeriesRenderer renderer2 = new XYSeriesRenderer();
        renderer2.setColor(Color.parseColor("#666600"));
        renderer2.setDisplayChartValues(true);

        XYSeriesRenderer renderer3 = new XYSeriesRenderer();
        renderer3.setColor(Color.parseColor("#FF0000"));
        renderer3.setDisplayChartValues(true);

        XYSeriesRenderer renderer4 = new XYSeriesRenderer();
        renderer4.setColor(Color.parseColor("#0000FF"));
        renderer4.setDisplayChartValues(true);

        XYSeriesRenderer renderer5 = new XYSeriesRenderer();
        renderer5.setColor(Color.parseColor("#6767D0"));
        renderer5.setDisplayChartValues(true);

        XYSeriesRenderer renderer6 = new XYSeriesRenderer();
        renderer6.setColor(Color.parseColor("#AAAA00"));
        renderer6.setDisplayChartValues(true);

        XYSeriesRenderer renderer7 = new XYSeriesRenderer();
        renderer7.setColor(Color.parseColor("#00AAAA"));
        renderer7.setDisplayChartValues(true);

        XYSeriesRenderer renderer8 = new XYSeriesRenderer();
        renderer8.setColor(Color.parseColor("#000000"));
        renderer8.setDisplayChartValues(true);

        mRenderer.addSeriesRenderer(renderer);
        mRenderer.addSeriesRenderer(renderer2);
        mRenderer.addSeriesRenderer(renderer3);
        mRenderer.addSeriesRenderer(renderer4);
        mRenderer.addSeriesRenderer(renderer5);
        mRenderer.addSeriesRenderer(renderer6);
        mRenderer.addSeriesRenderer(renderer7);
        mRenderer.addSeriesRenderer(renderer8);

        return ChartFactory.getBarChartView(getActivity(), dataset,mRenderer, Type.DEFAULT);
	}

	private GraphicalView getBarChartView() {
		String[] titles = new String[] { "2007", "2008" };
	    List<double[]> values = new ArrayList<double[]>();
	    values.add(new double[] { 5230, 7300, 9240, 10540, 7900, 9200, 12030, 11200, 9500, 10500, 11600, 13500 });
	    values.add(new double[] { 14230, 12300, 14240, 15244, 15900, 19200, 22030, 21200, 19500, 15500, 12600, 14000 });
	    int[] colors = new int[] { Color.CYAN, Color.BLUE };
	    XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
	    renderer.setOrientation(Orientation.VERTICAL);
	    setChartSettings(renderer, "Monthly sales in the last 2 years", "Month", "Units sold", 0.5, 12.5, 0, 24000, Color.GRAY, Color.LTGRAY);
	    renderer.setXLabels(1);
	    renderer.setYLabels(10);
	    renderer.addXTextLabel(1, "Jan");
	    renderer.addXTextLabel(3, "Mar");
	    renderer.addXTextLabel(5, "May");
	    renderer.addXTextLabel(7, "Jul");
	    renderer.addXTextLabel(10, "Oct");
	    renderer.addXTextLabel(12, "Dec");
	    int length = renderer.getSeriesRendererCount();
	    for (int i = 0; i < length; i++) {
	      SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(i);
	      seriesRenderer.setDisplayChartValues(true);
	    }
	    return ChartFactory.getBarChartView(getActivity(), buildBarDataset(titles, values), renderer, Type.DEFAULT);
	}

	private Intent getBarChartIntent() {
		String[] titles = new String[] { "2007", "2008" };
	    List<double[]> values = new ArrayList<double[]>();
	    values.add(new double[] { 5230, 7300, 9240, 10540, 7900, 9200, 12030, 11200, 9500, 10500, 11600, 13500 });
	    values.add(new double[] { 14230, 12300, 14240, 15244, 15900, 19200, 22030, 21200, 19500, 15500, 12600, 14000 });
	    int[] colors = new int[] { Color.CYAN, Color.BLUE };
	    XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
	    renderer.setOrientation(Orientation.VERTICAL);
	    setChartSettings(renderer, "Monthly sales in the last 2 years", "Month", "Units sold", 0.5, 12.5, 0, 24000, Color.GRAY, Color.LTGRAY);
	    renderer.setXLabels(1);
	    renderer.setYLabels(10);
	    renderer.addXTextLabel(1, "Jan");
	    renderer.addXTextLabel(3, "Mar");
	    renderer.addXTextLabel(5, "May");
	    renderer.addXTextLabel(7, "Jul");
	    renderer.addXTextLabel(10, "Oct");
	    renderer.addXTextLabel(12, "Dec");
	    int length = renderer.getSeriesRendererCount();
	    for (int i = 0; i < length; i++) {
	      SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(i);
	      seriesRenderer.setDisplayChartValues(true);
	    }
	    return ChartFactory.getBarChartIntent(getActivity(), buildBarDataset(titles, values), renderer, Type.DEFAULT);
	}

	  /**
	   * Builds a bar multiple series dataset using the provided values.
	   *
	   * @param titles the series titles
	   * @param values the values
	   * @return the XY multiple bar dataset
	   */
	  protected XYMultipleSeriesDataset buildBarDataset(String[] titles, List<double[]> values) {
	    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
	    int length = titles.length;
	    for (int i = 0; i < length; i++) {
	      CategorySeries series = new CategorySeries(titles[i]);
	      double[] v = values.get(i);
	      int seriesLength = v.length;
	      for (int k = 0; k < seriesLength; k++) {
	        series.add(v[k]);
	      }
	      dataset.addSeries(series.toXYSeries());
	    }
	    return dataset;
	  }

	  /**
	   * Builds a bar multiple series renderer to use the provided colors.
	   *
	   * @param colors the series renderers colors
	   * @return the bar multiple series renderer
	   */
	  protected XYMultipleSeriesRenderer buildBarRenderer(int[] colors) {
	    XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
	    renderer.setAxisTitleTextSize(16);
	    renderer.setChartTitleTextSize(20);
	    renderer.setLabelsTextSize(15);
	    renderer.setLegendTextSize(15);
	    int length = colors.length;
	    for (int i = 0; i < length; i++) {
	      SimpleSeriesRenderer r = new SimpleSeriesRenderer();
	      r.setColor(colors[i]);
	      renderer.addSeriesRenderer(r);
	    }
	    return renderer;
	  }

	  protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
		      String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor,
		      int labelsColor) {
		    renderer.setChartTitle(title);
		    renderer.setXTitle(xTitle);
		    renderer.setYTitle(yTitle);
		    renderer.setXAxisMin(xMin);
		    renderer.setXAxisMax(xMax);
		    renderer.setYAxisMin(yMin);
		    renderer.setYAxisMax(yMax);
		    renderer.setAxesColor(axesColor);
		    renderer.setLabelsColor(labelsColor);
		  }


	@Override
	public void onGraphQuerySuccess(List<Receipt> receipts) {
		// TODO Auto-generated method stub

	}

}
