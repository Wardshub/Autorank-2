package me.armar.plugins.autorank.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;

import com.google.common.collect.Lists;

import me.armar.plugins.autorank.Autorank;

/**
 * This class is used to talk to the Paths.yml file. It allows creation,
 * deleting and saving of the Paths.yml. <br>
 * It is used to retrieve properties of paths, whether it may be requirements,
 * prerequisites or results.
 * 
 * @author Staartvin
 *
 */
public class PathsConfig {

    private SimpleYamlConfiguration config;
    private String fileName = "Paths.yml";

    private Autorank plugin;

    public PathsConfig(Autorank instance) {
        this.plugin = instance;
    }

    /**
     * Create a new Paths.yml file (if it doesn't exist) and load it.
     */
    public void createNewFile() {
        config = new SimpleYamlConfiguration(plugin, fileName, fileName);

        loadConfig();

        plugin.getLogger().info("Paths file loaded (" + fileName + ")");
    }

    /**
     * Get the Paths.yml file.
     * 
     * @return Paths.yml file or null if it doesn't exist
     */
    public FileConfiguration getConfig() {
        if (config != null) {
            return config;
        }

        return null;
    }

    /**
     * Reload Paths.yml config.
     */
    public void reloadConfig() {
        if (config != null) {
            config.reloadFile();
        }
    }

    /**
     * Save Paths.yml config.
     */
    public void saveConfig() {
        if (config == null) {
            return;
        }

        config.saveFile();
    }

    /**
     * Check whether a certain path can be done over and over again.
     * 
     * @param pathName
     *            Name of path to check
     * @return true if a player can do a path infinitely many times, false
     *         otherwise.
     */
    public boolean allowInfinitePathing(String pathName) {
        return this.getConfig().getBoolean(pathName + ".options.infinite pathing", false);
    }

    /**
     * Get the display name of this path. Will return the regular path name if
     * no display name was specified.
     * 
     * @param pathName
     *            Name of the path
     * @return display name of the path or the path name itself if it doesn't
     *         exist.
     */
    public String getDisplayName(final String pathName) {
        return this.getConfig().getString(pathName + ".options.display name", pathName);
    }

    /**
     * Get a list of paths that are defined in the Paths.yml
     * 
     * @return a list of path names.
     */
    public List<String> getPaths() {
        return new ArrayList<String>(getConfig().getKeys(false));
    }

    /**
     * Get the ID of a prerequisite for a certain path
     * 
     * @param pathName
     *            Name of the path
     * @param prereqName
     *            Name of the prerequisite
     * @return the id of the prerequisite or -1.
     */
    public int getPrereqId(final String pathName, final String prereqName) {
        final Object[] reqs = getPrerequisites(pathName).toArray();

        for (int i = 0; i < reqs.length; i++) {
            final String prereqString = (String) reqs[i];

            if (prereqName.equalsIgnoreCase(prereqString)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Get the value strings for the specified prerequisite. Most of the times,
     * this will be just a single value string. <br>
     * These value strings are used to initialise the paths.
     * 
     * @param pathName
     *            Name of the path
     * @param prereqName
     *            Name of the prerequisite
     * @return a list of value strings or an empty list.
     */
    public List<String[]> getPrerequisiteOptions(final String pathName, final String prereqName) {
        // Grab options from string
        final String org = this.getPrerequisiteValue(pathName, prereqName);

        final List<String[]> list = new ArrayList<String[]>();

        final String[] split = org.split(",");

        for (final String sp : split) {
            final StringBuilder builder = new StringBuilder(sp);

            if (builder.charAt(0) == '(') {
                builder.deleteCharAt(0);
            }

            if (builder.charAt(builder.length() - 1) == ')') {
                builder.deleteCharAt(builder.length() - 1);
            }

            final String[] splitArray = builder.toString().trim().split(";");
            list.add(splitArray);
        }

        return list;
    }

    /**
     * Get a list of prerequisites for a specific path.
     * 
     * @param pathName
     *            Name of path
     * @return a list of all the names of the prerequisites.
     */
    public List<String> getPrerequisites(String pathName) {
        return new ArrayList<String>(getConfig().getConfigurationSection(pathName + ".prerequisites").getKeys(false));
    }

    /**
     * Get the value string that is associated with the given prerequisite for a
     * given path. <br>
     * This is used with {@link #getPrerequisiteOptions(String, String)}.
     * 
     * @param pathName
     *            Name of the path.
     * @param prereqName
     *            Name of the prerequisite.
     * @return the value string which can be null if none is specified.
     */
    public String getPrerequisiteValue(final String pathName, final String prereqName) {

        // Correct config
        String result;
        result = (this.getConfig().get(pathName + ".prerequisites." + prereqName + ".value") != null)
                ? this.getConfig().get(pathName + ".prerequisites." + prereqName + ".value").toString()
                : this.getConfig().getString(pathName + ".prerequisites." + prereqName).toString();

        return result;
    }

    /**
     * Get the ID of a requirement for a certain path
     * 
     * @param pathName
     *            Name of the path
     * @param reqName
     *            Name of the requirement
     * @return requirement id or -1 if none was found.
     */
    public int getReqId(final String pathName, final String reqName) {
        final Object[] reqs = getRequirements(pathName).toArray();

        for (int i = 0; i < reqs.length; i++) {
            final String reqString = (String) reqs[i];

            if (reqName.equalsIgnoreCase(reqString)) {
                return i;
            }
        }

        return -1;
    }
    
    /**
     * Get the name of a requirement that corresponds to the given requirement id.
     * @param pathName Name of the path.
     * @param reqId Id of the requirement
     * @return Name of the requirement that matches this id, or null if it doesn't exist.
     */
    public String getRequirementName(String pathName, int reqId) {
        List<String> reqs = this.getRequirements(pathName);
        
        if (reqId < 0 || reqId >= reqs.size()) {
            return null;
        }
        
        return reqs.get(reqId);
    }
    
    /**
     * Get the name of a prerequisite that corresponds to the given prerequisite id.
     * @param pathName Name of the path.
     * @param prereqId Id of the prerequisite
     * @return Name of the prerequisite that matches this id, or null if it doesn't exist.
     */
    public String getPrerequisiteName(String pathName, int prereqId) {
        List<String> preReqs = this.getPrerequisites(pathName);
        
        if (prereqId < 0 || prereqId >= preReqs.size()) {
            return null;
        }
        
        return preReqs.get(prereqId);
    }

    /**
     * Get the value strings for the specified requirement. Most of the times,
     * this will be just a single value string. <br>
     * These value strings are used to initialise the paths.
     * 
     * @param pathName
     *            Name of the path
     * @param reqName
     *            Name of the requirement
     * @return a list of value strings or an empty list.
     */
    public List<String[]> getRequirementOptions(final String pathName, final String reqName) {
        // Grab options from string
        final String org = this.getRequirementValue(pathName, reqName);

        final List<String[]> list = new ArrayList<String[]>();

        final String[] split = org.split(",");

        for (final String sp : split) {
            final StringBuilder builder = new StringBuilder(sp);

            if (builder.charAt(0) == '(') {
                builder.deleteCharAt(0);
            }

            if (builder.charAt(builder.length() - 1) == ')') {
                builder.deleteCharAt(builder.length() - 1);
            }

            final String[] splitArray = builder.toString().trim().split(";");
            list.add(splitArray);
        }

        return list;
    }

    /**
     * Get a list of requirements for a specific path.
     * 
     * @param pathName
     *            Name of path
     * @return a list of all the names of the requirements.
     */
    public List<String> getRequirements(String pathName) {
        return new ArrayList<String>(getConfig().getConfigurationSection(pathName + ".requirements").getKeys(false));
    }

    /**
     * Get the value string that is associated with the given requirement for a
     * given path. <br>
     * This is used with {@link #getRequirementOptions(String, String)}.
     * 
     * @param pathName
     *            Name of the path.
     * @param reqName
     *            Name of the requirement.
     * @return the value string which can be null if none is specified.
     */
    public String getRequirementValue(final String pathName, final String reqName) {
        // Check if there is a value for PathName.requirements.RequirementName.Value
        // If not, use PathName.requirements.RequirementName (without the Value).
        String result = (this.getConfig().get(pathName + ".requirements." + reqName + ".value") != null)
                ? this.getConfig().get(pathName + ".requirements." + reqName + ".value").toString()
                : this.getConfig().getString(pathName + ".requirements." + reqName).toString();

        return result;
    }

    /**
     * Get the result string for a specific result of a path.
     * 
     * @param pathName
     *            Name of path
     * @param resultName
     *            Name of the result
     * @return the value string or null if it doesn't exist.
     */
    public String getResultOfPath(String pathName, String resultName) {
        // Correct config
        String result;
        result = (this.getConfig().get(pathName + ".results." + resultName + ".value") != null)
                ? this.getConfig().get(pathName + ".results." + resultName + ".value").toString()
                : this.getConfig().getString(pathName + ".results." + resultName).toString();

        return result;
    }

    /**
     * Get the result string for a specific result of a specific requirement of
     * a path.
     * 
     * @param pathName
     *            Name of the path
     * @param reqName
     *            Name of the requirement
     * @param resName
     *            Name of the result
     * @return the value string or null if it doesn't exist.
     */
    public String getResultOfRequirement(final String pathName, final String reqName, final String resName) {
        // Check if there is a value for PathName.requirements.RequirementName.results.ResultName.Value
        // If not, use PathName.requirements.RequirementName.results.ResultName (without the Value).
        String result = (this.getConfig().get(pathName + ".requirements." + reqName + ".results." + resName + ".value") != null)
                ? this.getConfig().get(pathName + ".requirements." + reqName + ".results." + resName + ".value").toString()
                : this.getConfig().getString(pathName + ".requirements." + reqName + ".results." + resName).toString();

        return result;
    }

    /**
     * Get all results of a path.
     * 
     * @param pathName
     *            Name of path
     * @return a list of names as results.
     */
    public List<String> getResults(String pathName) {
        return new ArrayList<String>(getConfig().getConfigurationSection(pathName + ".results").getKeys(false));
    }

    /**
     * Get all results names for a specific requirement of a path.
     * 
     * @param pathName
     *            Name of path
     * @param reqName
     *            Name of requirement
     * @return a list of names that correspond with results or an empty list.
     */
    public List<String> getResultsOfRequirement(final String pathName, final String reqName) {
        Set<String> results = new HashSet<String>();

        results = (getConfig().getConfigurationSection(pathName + ".requirements." + reqName + ".results") != null)
                ? getConfig().getConfigurationSection(pathName + ".requirements." + reqName + ".results").getKeys(false)
                : new HashSet<String>();

        return Lists.newArrayList(results);
    }

    /**
     * Get the value of the 'world' option. This option is used to specify
     * whether a requirement should hold on a given world.
     * 
     * @param pathName
     *            Name of path
     * @param reqName
     *            Name of the requirement
     * @return the value string of the 'world' option, or null if it doesn't
     *         exist.
     */
    public String getWorldOfRequirement(String pathName, String reqName) {
        return this.getConfig().getString(pathName + ".requirements." + reqName + ".options.world", null);
    }

    /**
     * Get whether a prerequisite is optional for a certain path
     * 
     * @param pathName
     *            Name of path
     * @param prereqName
     *            Name of prerequisite
     * @return true if optional; false otherwise
     */
    public boolean isOptionalPrerequisite(final String pathName, final String prereqName) {
        final boolean optional = getConfig().getBoolean(pathName + ".prerequisites." + prereqName + ".options.optional",
                false);

        return optional;
    }

    /**
     * Get whether a requirement is optional for a certain path
     * 
     * @param pathName
     *            Name of path
     * @param reqName
     *            Name of requirement
     * @return true if optional; false otherwise
     */
    public boolean isOptionalRequirement(final String pathName, final String reqName) {
        final boolean optional = getConfig().getBoolean(pathName + ".requirements." + reqName + ".options.optional",
                false);

        return optional;
    }

    /**
     * Get whether a requirement is world specific. For more info, see
     * {@link #getWorldOfRequirement(String, String)}.
     * 
     * @param pathName
     *            Name of path
     * @param reqName
     *            Name of requirement
     * @return true if the given requirement has a 'world' option. False
     *         otherwise.
     */
    public boolean isRequirementWorldSpecific(String pathName, String reqName) {
        return this.getWorldOfRequirement(pathName, reqName) != null;
    }

    /**
     * Load the Paths.yml file.
     */
    public void loadConfig() {

    }

    /**
     * Get whether Autorank should auto complete for a certain requirement in a
     * path. <br>
     * If auto completion is turned on for a requirement, Autorank will mark it
     * as done when it detects that a player meets the requirement. Autorank
     * will also perform the results of that requirement.
     * 
     * @param pathName
     *            Name of path
     * @param reqName
     *            Name of requirement
     * @return true if auto completion is turned on, false otherwise.
     */
    public boolean useAutoCompletion(final String pathName, final String reqName) {
        final boolean optional = isOptionalRequirement(pathName, reqName);

        if (optional) {
            // Not defined (Optional + not defined = false)
            if (this.getConfig().get(pathName + ".requirements." + reqName + ".options.auto complete") == null) {
                return false;
            } else {
                // Defined (Optional + defined = defined)
                return this.getConfig().getBoolean(pathName + ".requirements." + reqName + ".options.auto complete");
            }
        } else {
            // Not defined (Not optional + not defined = true)
            if (this.getConfig().get(pathName + ".requirements." + reqName + ".options.auto complete") == null) {

                // If partial completion is false, we do not auto complete
                /*
                 * if (!usePartialCompletion()) { return false; }
                 */
                return true;
            } else {
                // Defined (Not optional + defined = defined)
                return this.getConfig().getBoolean(pathName + ".requirements." + reqName + ".options.auto complete");
            }
        }
    }
    
    /**
     * Get the results that should be performed when a user chooses a given path.
     * @param pathName Name of the path
     * @return a list of result names that should be used performed
     */
    public ArrayList<String> getResultsUponChoosing(String pathName) {
        Set<String> results = new HashSet<String>();

        results = (getConfig().getConfigurationSection(pathName + ".upon choosing") != null)
                ? getConfig().getConfigurationSection(pathName + ".upon choosing").getKeys(false)
                : new HashSet<String>();

        return Lists.newArrayList(results);
    }
    
    /**
     * Get the result value of the given result.
     * @param pathName Name of the path
     * @param resName Name of the result
     * @return the string value of this result or null if it doesn't exist.
     */
    public String getResultValueUponChoosing(final String pathName, final String resName) {
     // Correct config
        String result;
        result = (this.getConfig().get(pathName + ".upon choosing." + resName + ".value") != null)
                ? this.getConfig().get(pathName + ".upon choosing." + resName + ".value").toString()
                : this.getConfig().getString(pathName + ".upon choosing." + resName).toString();

        return result;
    }
    
    /**
     * Check whether Autorank should automatically assign the given path to a player when he meets the prerequisites.
     * @param pathName Name of the path
     * @return true if Autorank should assign the given path to the player, false otherwise.
     */
    public boolean shouldAutoChoosePath(String pathName) {
        return this.getConfig().getBoolean(pathName + ".options.auto choose", false);
    }
    
    /**
     * Get the priority of a given path. The priority of a path is used to determine which path Autorank 
     * should automatically assign to a player. The priority is a positive integer (unbounded). By default, all paths have a priority of 1.
     * @param pathName Name of the path
     * @return a positive integer representing the priority of the path. By default the priority is 1.
     */
    public int getPriorityOfPath(String pathName) {
        return this.getConfig().getInt(pathName + ".options.priority", 1);
    }
    
    /**
     * Check whether Autorank should show the given path in the list of possible paths based on whether a player meets the prerequisites of the path.
     * @param pathName Name of the path
     * @return true if Autorank should only show the given path if the player meets the path's prerequisites, false otherwise.
     */
    public boolean showBasedOnPrerequisites(String pathName) {
        return this.getConfig().getBoolean(pathName + ".options.show based on prerequisites", false);
    }
}
