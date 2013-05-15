package vroom.common.modeling.vrprep.translations;

/**
 * Defines the default method(s) that must be used by the translator classes
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a>
 *
 */
public interface Translator {
	
	/**
	 * Default method used as main method to translate file
	 * @param file file to translate
	 */
	public void translateFile(String file);

}
