package de.uni_potsdam.hpi.kpp.parallel_grep_java;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchThread extends Thread {
	private final Grep grep;
	private final String searchString;
	
	public SearchThread(Grep grep, String searchString) {
		this.grep = grep;
		this.searchString = searchString;
	}
	
	@Override
	public void run() {
		int occurences = 0;

		Pattern p = Pattern.compile(searchString);
		Matcher m = p.matcher(grep.input);
		while (m.find()) {
			occurences++;
		}

		grep.writeResultAndStartNewThread(searchString, occurences);
	}
}
