package co.smartreceipts.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.model.Columns;
import co.smartreceipts.android.model.Columns.Column;
import co.smartreceipts.android.persistence.DatabaseHelper;

@Config(emulateSdk = 18) 
@RunWith(RobolectricTestRunner.class)
public class ColumnsDBTest {

	private SmartReceiptsApplication mApp;
	private DatabaseHelper mDB;
	
	@Before
	public void setup() {
		mApp = (SmartReceiptsApplication) Robolectric.application;
		mDB = mApp.getPersistenceManager().getDatabase();
	}
	
	@After
	public void tearDown() {
		mDB.close();
		mDB = null;
		mApp = null;
	}
	
	@Test
	public void getCSV() {
		final Columns columns = mDB.getCSVColumns();
		assertNotNull(columns);
		assertTrue(columns.size() > 0);
	}
	
	@Test
	public void getPDF() {
		final Columns columns = mDB.getPDFColumns();
		assertNotNull(columns);
		assertTrue(columns.size() > 0);
	}
	
	@Test
	public void insertCSV() {
		final Columns oldColumns = mDB.getCSVColumns();
		final int oldSize = oldColumns.size();
		assertTrue(mDB.insertCSVColumn());
		final Columns newColumns = mDB.getCSVColumns();
		assertEquals(oldSize + 1, newColumns.size());
		assertEquals(oldColumns, newColumns);
		final int lastIdx = newColumns.size() - 1;
		final Column lastCol = newColumns.get(lastIdx);
		assertEquals(lastCol.getColumnType(), Columns.BLANK(mApp.getFlex()));
		assertEquals(lastCol.getIndex(), lastIdx + 1); //1-th indexed (not 0)
	}
	
	@Test
	public void insertPDF() {
		final Columns oldColumns = mDB.getPDFColumns();
		final int oldSize = oldColumns.size();
		assertTrue(mDB.insertPDFColumn());
		final Columns newColumns = mDB.getPDFColumns();
		assertEquals(oldSize + 1, newColumns.size());
		assertEquals(oldColumns, newColumns);
		final int lastIdx = newColumns.size() - 1;
		final Column lastCol = newColumns.get(lastIdx);
		assertEquals(lastCol.getColumnType(), Columns.BLANK(mApp.getFlex()));
		assertEquals(lastCol.getIndex(), lastIdx + 1); //1-th indexed (not 0)
	}
	
	@Test
	public void insertCSVNoCache() {
		final Columns columns = mDB.getCSVColumns();
		assertTrue(mDB.insertCSVColumnNoCache(Columns.CATEGORY_CODE(mApp.getFlex())));
		final int lastIdx = columns.size() - 1;
		final Column lastCol = columns.get(lastIdx);
		assertNotEquals(lastCol.getColumnType(), Columns.CATEGORY_CODE(mApp.getFlex()));
	}
	
	@Test
	public void insertPDFNoCache() {
		Columns columns = mDB.getPDFColumns();
		assertTrue(mDB.insertPDFColumnNoCache(Columns.CATEGORY_CODE(mApp.getFlex())));
		final int lastIdx = columns.size() - 1;
		final Column lastCol = columns.get(lastIdx);
		assertNotEquals(lastCol.getColumnType(), Columns.CATEGORY_CODE(mApp.getFlex()));
	}
	
	@Test
	public void insertCSVFirst() {
		assertTrue(mDB.insertCSVColumn());
		final Columns newColumns = mDB.getCSVColumns();
		final int lastIdx = newColumns.size() - 1;
		final Column lastCol = newColumns.get(lastIdx);
		assertEquals(lastCol.getColumnType(), Columns.BLANK(mApp.getFlex()));
		assertEquals(lastCol.getIndex(), lastIdx + 1);
	}
	
	@Test
	public void insertPDFFirst() {
		assertTrue(mDB.insertPDFColumn());
		final Columns newColumns = mDB.getPDFColumns();
		final int lastIdx = newColumns.size() - 1;
		final Column lastCol = newColumns.get(lastIdx);
		assertEquals(lastCol.getColumnType(), Columns.BLANK(mApp.getFlex()));
		assertEquals(lastCol.getIndex(), lastIdx + 1); 
	}
	
	@Test
	public void updateCSV() {
		final Columns oldColumns = mDB.getCSVColumns();
		final int optionIndex = 5;
		final String newColString = oldColumns.getSpinnerOptionAt(optionIndex);
		final int lastIdx = oldColumns.size() - 1;
		final int oldSize = oldColumns.size();
		assertTrue(mDB.updateCSVColumn(lastIdx, optionIndex));
		final Columns newColumns = mDB.getCSVColumns();
		assertEquals(oldSize, newColumns.size());
		assertEquals(oldColumns, newColumns);
		final Column lastCol = newColumns.get(lastIdx);
		assertEquals(lastCol.getColumnType(), newColString);
		assertTrue(mDB.updateCSVColumn(lastIdx, optionIndex)); // Repeat update but with no change
	}
	
	@Test
	public void updatePDF() {
		final Columns oldColumns = mDB.getPDFColumns();
		final int optionIndex = 5;
		final String newColString = oldColumns.getSpinnerOptionAt(optionIndex);
		final int lastIdx = oldColumns.size() - 1;
		final int oldSize = oldColumns.size();
		assertTrue(mDB.updatePDFColumn(lastIdx, optionIndex));
		final Columns newColumns = mDB.getPDFColumns();
		assertEquals(oldSize, newColumns.size());
		assertEquals(oldColumns, newColumns);
		final Column lastCol = newColumns.get(lastIdx);
		assertEquals(lastCol.getColumnType(), newColString);
		assertTrue(mDB.updatePDFColumn(lastIdx, optionIndex)); // Repeat update but with no change
	}
	
	@Test
	public void deleteCSV() {
		final Columns oldColumns = mDB.getCSVColumns();
		final int oldSize = oldColumns.size();
		final List<Column> columnsList = new ArrayList<Column>(oldSize - 1);
		for (int i=0; i < oldSize - 1; i++) {
			columnsList.add(oldColumns.get(i));
		}
		assertTrue(mDB.deleteCSVColumn());
		final Columns newColumns = mDB.getCSVColumns();
		assertEquals(oldSize - 1, newColumns.size());
		assertEquals(oldColumns, newColumns);
		for (int i=0; i < newColumns.size(); i++) {
			assertEquals(columnsList.get(i).getColumnType(), newColumns.get(i).getColumnType());
			assertEquals(columnsList.get(i).getIndex(), newColumns.get(i).getIndex());
		}
	}
	
	@Test
	public void deletePDF() {
		final Columns oldColumns = mDB.getPDFColumns();
		final int oldSize = oldColumns.size();
		final List<Column> columnsList = new ArrayList<Column>(oldSize - 1);
		for (int i=0; i < oldSize - 1; i++) {
			columnsList.add(oldColumns.get(i));
		}
		assertTrue(mDB.deletePDFColumn());
		final Columns newColumns = mDB.getPDFColumns();
		assertEquals(oldSize - 1, newColumns.size());
		assertEquals(oldColumns, newColumns);
		for (int i=0; i < newColumns.size(); i++) {
			assertEquals(columnsList.get(i).getColumnType(), newColumns.get(i).getColumnType());
			assertEquals(columnsList.get(i).getIndex(), newColumns.get(i).getIndex());
		}
	}
	
}
