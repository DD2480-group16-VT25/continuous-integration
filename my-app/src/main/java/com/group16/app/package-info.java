/**
 * This is a simple CI server for the Assignment #2: Continuous Integration
 * which when receiving a webhook from GitHub checks out the commit, runs
 * mvn compile and mvn test and then sends a status notification to the
 * GitHub commit in question.
 * @author Ellen Sigurðardóttir
 * @author Linus Dinesjö
 * @author Marcus Odin
 * @author Robin Gunnarson
 * @version 2025-02-12
 */
package com.group16.app;
