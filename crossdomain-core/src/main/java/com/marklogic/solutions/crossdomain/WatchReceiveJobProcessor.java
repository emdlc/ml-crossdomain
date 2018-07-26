package com.marklogic.solutions.crossdomain;

import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import org.apache.log4j.Logger;
import org.jdom2.Namespace;

	public class WatchReceiveJobProcessor extends JobProcessor<String> {
 		    private WatchService watcher;
		    private Map<WatchKey,Path> keys;
		    private boolean recursive;
		    private boolean trace = false;
			private static final Logger logger = Logger.getLogger(WatchReceiveJobProcessor.class);
			private int BATCHSIZE;
			private static String landingZoneDir;
			private String landingZoneArchiveDir;
			private String landingZoneErrorDir;
			private String xccURL;
			private String collection;
			private Namespace cds = Namespace.getNamespace("p","http://marklogic.com/mlcs/cds");
			private Boolean jarErroredFlag = false; 
			private String jarFileName = null;
			private String jarFileFullPathString = null;
						
		    @SuppressWarnings("unchecked")
		    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		        return (WatchEvent<T>)event;
		    }

		    /**
		     * Register the given directory with the WatchService
		     */
		    private void register(Path dir) throws IOException {
		    	WatchKey key = dir.register(watcher, ENTRY_CREATE);
		        if (trace) {
		            Path prev = keys.get(key);
		            if (prev == null) {
		                System.out.format("register: %s\n", dir);
		            } else {
		                if (!dir.equals(prev)) {
		                    System.out.format("update: %s -> %s\n", prev, dir);
		                }
		            }
		        }
		        keys.put(key, dir);
		    }

		    /**
		     * Register the given directory, and all its sub-directories, with the
		     * WatchService.
		     */
		    private void registerAll(final Path start) throws IOException {
		        // register directory and sub-directories
		    	logger.error("inside registerAll with Path " + start);
		        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
		            @Override
		            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
		                throws IOException
		            {
		                register(dir);
		                return FileVisitResult.CONTINUE;
		            }
		        });
		    }

		    /**
		     * Creates a WatchService and registers the given directory
		     */
		    WatchReceiveJobProcessor(Path dir, boolean recursive) throws IOException {
		        this.watcher = FileSystems.getDefault().newWatchService();
		        this.keys = new HashMap<WatchKey,Path>();
		        this.recursive = recursive;

		        if (recursive) {
		            System.out.format("Scanning %s ...\n", dir);
		            registerAll(dir);
		            System.out.println("Done.");
		        } else {
		            register(dir);
		        }

		        // enable trace after initial registration
		        this.trace = true;
		    }

		    public WatchReceiveJobProcessor() {
		    		super();
		    		
		    		Properties prop = new Properties();
		    		InputStream input = null;
		    		
		    		try {			
		    			String filename = "receiveJob.properties";
		    			input = WatchReceiveJobProcessor.class.getClassLoader().getResourceAsStream(filename);
		        		if(input==null) {
		        			logger.error("unable to find file:" + filename);
		        			return;
		        		}
		    			
		    			// load a properties file
		    			prop.load(input);
		    			this.landingZoneDir=prop.getProperty("landingzone.dir");
		    			
		    		} catch (IOException e) {
		    			e.printStackTrace();
		    		} finally {
		    			if (input != null) {
		    					try {
		    						input.close();
		    					} catch (IOException e) {
		    						e.printStackTrace();
		    					}
		    			}
		    		}
			}

			/**
		     * Process all events for keys queued to the watcher
		     */
		    void processEvents() {
		        for (;;) {

		            // wait for key to be signalled
		            WatchKey key;
		            try {
		                key = watcher.take();
		            } catch (InterruptedException x) {
		                return;
		            }

		            Path dir = keys.get(key);
		            if (dir == null) {
		                System.err.println("WatchKey not recognized!!");
		                continue;
		            }

		            for (WatchEvent<?> event: key.pollEvents()) {
		                WatchEvent.Kind kind = event.kind();

		                // TBD - provide example of how OVERFLOW event is handled
		                if (kind == OVERFLOW) {
		                    continue;
		                }

		                // Context for directory entry event is the file name of entry
		                WatchEvent<Path> ev = cast(event);
		                Path name = ev.context();
		                Path child = dir.resolve(name);
		                // print out event
		                System.out.format("%s: %s\n", event.kind().toString(), child);
		                
		                //added for CDS specific but look below at next if statement
		                if ( kind == ENTRY_CREATE ) {
		                	SimpleJobRunManager<String> mgr = new SimpleJobRunManager<String>(new ReceiveJobProcessor());
		                	JobResult result = mgr.runJob();
						}

		                // if directory is created, and watching recursively, then
		                // register it and its sub-directories
		                if (recursive && (kind == ENTRY_CREATE)) {
		                    try {
		                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
		                            registerAll(child);
		                        }
		                    } catch (IOException x) {
		                        // ignore to keep sample readbale
		                    }
		                }
		            }

		            // reset key and remove from set if directory no longer accessible
		            boolean valid = key.reset();
		            if (!valid) {
		                keys.remove(key);

		                // all directories are inaccessible
		                if (keys.isEmpty()) {
		                    break;
		                }
		            }
		        }
		    }

			@Override
			public JobResult executeJob() {
				return null;
			}

			@Override
			protected ArrayBlockingQueue<String> retrieve(Map<String, Object> params) {
				Path dir = Paths.get(landingZoneDir);
		        try {
					new WatchReceiveJobProcessor(dir, true).processEvents();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}

			private Path Path(String landingZoneDir2) {
				// TODO Auto-generated method stub
				return null;
			}
	}
