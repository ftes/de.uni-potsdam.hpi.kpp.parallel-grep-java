package de.uni_potsdam.hpi.kpp.parallel_grep_java;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchThread extends Thread {
	private final Grep grep;
	private final String searchString;
	private final String input;
	
	public SearchThread(Grep grep, String searchString, String input) {
		this.grep = grep;
		this.searchString = searchString;
		this.input = input;
	}
	
	@Override
	public void run() {
		int occurences = 0;

		Pattern p = Pattern.compile(searchString);
		Matcher m = p.matcher(input);
		while (m.find()) {
			occurences += 1;
		}

		grep.writeResult(searchString, occurences);
	}
}
