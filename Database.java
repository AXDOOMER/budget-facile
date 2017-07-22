// Copyright (c) 2017 Alexandre-Xavier Labonté-Lamoureux
//
// Permission to use, copy, modify, and distribute this software for any
// purpose with or without fee is hereby granted, provided that the above
// copyright notice and this permission notice appear in all copies.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
// WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
// ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
// WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
// ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
// OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

// Database manager with persistant connection
public class Database {

	private Connection conn = null;
	public enum DB { SQLITE, DERBY, POSTGRE };
	private DB DBtype;

	public Database(String url) {
		this.connect(url);
		if (this.checkFilled() < 1) {
			this.fill();
		}
	}

	private void closeQuietly(AutoCloseable resource) {
		try {
			if (resource != null) {
				resource.close();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	// Connect to the database
	private void connect(String url) {
		if (conn == null) {
			try {
				// Create a connection to the database
				conn = DriverManager.getConnection(url);

				System.out.println("Connection established.");

				// Enable foreign keys for SQLite 3
				if (url.startsWith("jdbc:sqlite:")) {
					DBtype = DB.SQLITE;
					conn.createStatement().execute("PRAGMA foreign_keys = ON");
				} else if (url.startsWith("jdbc:derby:")) {
					DBtype = DB.DERBY;
				} else if (url.startsWith("jdbc:postgresql:")) {
					DBtype = DB.POSTGRE;
				} else {
					System.err.println("Could not guess the type of the database from the url.");
					System.exit(1);
				}

			} catch (SQLException e) {
				System.err.println(e);
			} /*finally {
				try {
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException ex) {
					System.out.println(ex.getMessage());
				}
			}*/
		} else {
			System.err.println("Connection already established.");
		}
	}

	// Check if the necessary tables are in the database
	private int checkFilled() {
		String sql = null;
		String column = null;	// Name of the column where the table names are
		if (DBtype == DB.SQLITE) {
			sql = "SELECT name FROM sqlite_master WHERE type='table'";
			column = "name";
		} else if (DBtype == DB.DERBY) {
			sql = "SELECT tablename FROM sys.systables WHERE tabletype='T'";
			column = "tablename";
		} else if (DBtype == DB.POSTGRE) {
			sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'";
			column = "table_name";
		}

		Statement stmt = null;
		ResultSet rs = null;
		int numTables = 0;
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);

			System.out.println("Tables found:");

			while (rs.next()) {
				System.out.println("* " + rs.getString(column));
				numTables++;
			}

			if (numTables == 0) {
				System.out.println("NONE!");
			}

			return numTables;

		} catch (SQLException e) {
			System.err.println(e);
		} finally {
			closeQuietly(stmt);
			closeQuietly(rs);
		}

		return numTables;
	}

	// Put the necessary tables in the database
	private void fill() {
		System.out.print("Filling database for the first time...");

		String id = null;
		if (DBtype == DB.SQLITE) {
			id = "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,";
		} else if (DBtype == DB.DERBY) {
			id = "id INTEGER PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),";
		}

		Statement stmt = null;
		try	{
			stmt = conn.createStatement();

			String categories = "CREATE TABLE categories ("
				+ id
				+ "	catname VARCHAR(20) NOT NULL,"
				+ "	description VARCHAR(50)"
			+ ")";

			stmt.execute(categories);
		} catch (SQLException e) {
			System.err.println(e);
		} finally {
			closeQuietly(stmt);
		}

		try	{
			stmt = conn.createStatement();

			String depenses = "CREATE TABLE depenses ("
				+ " " + id
				+ "	name VARCHAR(30) NOT NULL,"
				+ "	place VARCHAR(30) NOT NULL,"
				+ "	cost DECIMAL(6,2) NOT NULL,"
				+ " category INTEGER NOT NULL,"
				+ " FOREIGN KEY(category) REFERENCES categories(id) ON DELETE CASCADE"
				+ ")";

			stmt.execute(depenses);
		} catch (SQLException e) {
			System.err.println(e);
		} finally {
			closeQuietly(stmt);
		}

		System.out.println(" done!");
	}
}
