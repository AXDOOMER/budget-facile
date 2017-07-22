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

import java.util.Scanner;
import java.util.InputMismatchException;

// Java Database Connectivity (JDBC)
public class Main {
	// Main
	public static void main(String[] args) {

		Database sqldb = null;
		System.out.println("Choose database: 1=SQLite, 2=Derby, 3=PostegreSQL.");

		Scanner sc = new Scanner(System.in);
		try {
			switch(sc.nextInt()) {
				case 1:
					sqldb = new Database("jdbc:sqlite:sqlite3.db");
					break;
				case 2:
					sqldb = new Database("jdbc:derby:derby.db;create=true");
					break;
				case 3:
					System.out.println("POSTGRES NOT SUPPORTED YET");
					System.exit(1);		// Kill!		
					sqldb = new Database("jdbc:postgresql:postgre.db");
					break;
				default:
					System.err.println("Invalid choice.");
			}
		} catch (InputMismatchException ime) {
			System.err.println("Invalid input.");
		} 
	}
}
