package net.filebot;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static net.filebot.util.RegularExpressions.*;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class Language implements Serializable {

	// code for intern use
	private final String code;

	// ISO 639-1 code
	private final String iso_639_1;

	// ISO 639-3 code (mostly identical to ISO 639-2/T)
	private final String iso_639_3;

	// ISO 639-2/B code
	private final String iso_639_2B;

	// ISO 3166-1 alpha-3 code
	private final String iso_3166_1;

	// Language name
	private final String[] names;

	public Language(String code, String iso_639_1, String iso_639_3, String iso_639_2B, String iso_3166_1, String[] names) {
		this.code = code;
		this.iso_639_1 = iso_639_1;
		this.iso_639_3 = iso_639_3;
		this.iso_639_2B = iso_639_2B;
		this.iso_3166_1 = iso_3166_1;
		this.names = names.clone();
	}

	public String getCode() {
		return code;
	}

	public String getISO2() {
		return iso_639_1; // 2-letter code
	}

	public String getISO3() {
		return iso_639_3; // 3-letter code
	}

	public String getISO3B() {
		return iso_639_2B; // alternative 3-letter code
	}

	public String getISO3166_1() { return iso_3166_1; // 3-letter code
	}

	public String getName() {
		return names[0];
	}

	public List<String> getNames() {
		return unmodifiableList(asList(names));
	}

	@Override
	public String toString() {
		return iso_639_3;
	}

	public Locale getLocale() {
		return new Locale(getISO2(), getISO3166_1());
	}

	public boolean matches(String code) {
		if (this.code.equalsIgnoreCase(code) || iso_639_1.equalsIgnoreCase(code) || iso_639_3.equalsIgnoreCase(code) || iso_639_2B.equalsIgnoreCase(code)) {
			return true;
		}
		for (String it : names) {
			if (it.equalsIgnoreCase(code) || code.toLowerCase().contains(it.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Language clone() {
		return new Language(code, iso_639_1, iso_639_3, iso_639_2B, iso_3166_1, names);
	}

	public static final Comparator<Language> ALPHABETIC_ORDER = new Comparator<Language>() {

		@Override
		public int compare(Language o1, Language o2) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	};

	public static Language getLanguage(String code) {
		if (code == null || code.isEmpty()) {
			return null;
		}

		try {
			String[] values = TAB.split(getProperty(code), 5);
			return new Language(code, values[0], values[1], values[2], values[3], TAB.split(values[4]));
		} catch (Exception e) {
			return null;
		}
	}

	public static List<Language> getLanguages(String... codes) {
		return stream(codes).map(Language::getLanguage).collect(toList());
	}

	public static Language getLanguage(Locale locale) {
		return locale == null ? null : findLanguage(locale.getLanguage());
	}

	public static Language findLanguage(String language) {
		return availableLanguages().stream().filter(it -> it.matches(language)).findFirst().orElse(null);
	}

	public static String getStandardLanguageCode(String lang) {
		try {
			return Language.findLanguage(lang).getISO3();
		} catch (Exception e) {
			return null;
		}
	}

	public static List<Language> availableLanguages() {
		String languages = getProperty("languages.ui");
		return getLanguages(COMMA.split(languages));
	}

	public static List<Language> commonLanguages() {
		String languages = getProperty("languages.common");
		return getLanguages(COMMA.split(languages));
	}

	public static List<Language> preferredLanguages() {
		// English | System language | common languages
		Stream<String> codes = Stream.of("en", Locale.getDefault().getLanguage());

		// append common languages
		codes = Stream.concat(codes, stream(COMMA.split(getProperty("languages.common")))).distinct();

		return codes.map(Language::getLanguage).collect(toList());
	}

	private static String getProperty(String key) {
		return ResourceBundle.getBundle(Language.class.getName()).getString(key);
	}
}
