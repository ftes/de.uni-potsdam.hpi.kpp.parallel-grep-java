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
	
	/**
	 * aka lookforit()
	 * in the last step, the thread releases monitor control within the synchronized {@link Grep#lockForWrite()}
	 * method, if the result data structure is currently being accessed
	 */
	@Override
	public void run() {
		int occurences = 0;

		Pattern p = Pattern.compile(searchString);
		Matcher m = p.matcher(grep.input);
		while (m.find()) {
			occurences++;
		}

		// in this step, the thread releases monitor control within the synchronized {@link Grep#lock()}
		grep.writeResult(searchString, occurences);
	}
}
