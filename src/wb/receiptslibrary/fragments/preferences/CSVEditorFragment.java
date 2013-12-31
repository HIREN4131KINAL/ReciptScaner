package wb.receiptslibrary.fragments.preferences;

import wb.receiptslibrary.CSVColumns;
import wb.receiptslibrary.R;
import wb.receiptslibrary.fragments.WBFragment;
import wb.receiptslibrary.persistence.DatabaseHelper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CSVEditorFragment extends WBFragment implements View.OnClickListener {

	private CSVColumns mCSVColumns;
	private LayoutInflater mInflater;
	private TableLayout mTableLayout;
	private Button mAddButton, mRemoveButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.csv_editor, container, false);
		mTableLayout = (TableLayout) rootView.findViewById(R.id.csv_editor_table);
		mAddButton = (Button) mTableLayout.findViewById(R.id.csv_editor_add_column);
		mRemoveButton = (Button) mTableLayout.findViewById(R.id.csv_editor_remove_column);
		mAddButton.setOnClickListener(this);
		mRemoveButton.setOnClickListener(this);
		mCSVColumns = getPersistenceManager().getDatabase().getCSVColumns();
		final int size = mCSVColumns.size();
		for (int i=0; i < size; i++) {
			mTableLayout.addView(inflateCSVRow(i));
		}
		mInflater = inflater;
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getSherlockActivity().getSupportActionBar().setTitle(R.string.csv_table_title);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.csv_editor_add_column) {
			getPersistenceManager().getDatabase().insertCSVColumn();
			// TODO: Update mCSVColumns
			mTableLayout.addView(inflateCSVRow(mCSVColumns.size() - 1));
		}
		else if (v.getId() == R.id.csv_editor_remove_column) {
			getPersistenceManager().getDatabase().deleteCSVColumn();
			// TODO: Update mCSVColumns
			mTableLayout.removeViews(mCSVColumns.size(), 1);
		} 
	}
	
	private final TableRow inflateCSVRow(int index) {
		TableRow row = (TableRow) mInflater.inflate(R.layout.csv_editor_row, null); //Start here
		TextView column = (TextView) row.findViewById(R.id.left);
		Spinner spinner = (Spinner) row.findViewById(R.id.right);
		final CSVColumnSelectionListener selectionListener = new CSVColumnSelectionListener(getPersistenceManager().getDatabase(), index);
		final ArrayAdapter<CharSequence> options = CSVColumns.getNewArrayAdapter(getSherlockActivity(), getFlex());
		spinner.setAdapter(options);
		String type = mCSVColumns.getType(index);
		int pos = options.getPosition(type);
		if (pos < 0) { //This was a customized, non-accessbile entry from FleXML
			options.add(type);
			spinner.setSelection(options.getPosition(type));
			spinner.setEnabled(false);
		}
		else {
			spinner.setSelection(pos);
		}
		spinner.setOnItemSelectedListener(selectionListener);
		column.setText("" + (index+1));
		return row;
    }
	
	private class CSVColumnSelectionListener implements AdapterView.OnItemSelectedListener {
		
    	private DatabaseHelper sDatabaseHelper;
    	private int sIndex;
    	private boolean sFirstCall; //During the Spinner Creation, onItemSelected() is automatically called. This boolean ignores the initial call
    	
    	public CSVColumnSelectionListener(DatabaseHelper db, int index) {
    		sDatabaseHelper = db; 
    		sIndex = index; 
    		sFirstCall = true;
		}
    	
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			if (sFirstCall) { //Ignore creation call
				sFirstCall = false;
				return;
			}
			sDatabaseHelper.updateCSVColumn(sIndex, position);
		}
		
		@Override 
		public void onNothingSelected(AdapterView<?> parent) {}
    	
    }
	
}
