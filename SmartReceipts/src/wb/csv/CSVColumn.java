package wb.csv;

public class CSVColumn {
	public int index;
	public String columnType;
	CSVColumn(int index, String columnType) {
		this.index = index;
		this.columnType = columnType;
	}
	public String toString() {return columnType;}
}
