package ca.mcgill.sel.ram.ui.views.message.handler.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mt4j.sceneManagement.ISceneChangeListener;
import org.mt4j.sceneManagement.SceneChangeEvent;

import ca.mcgill.sel.commons.emf.util.AdapterFactoryRegistry;
import ca.mcgill.sel.commons.emf.util.ResourceManager;
import ca.mcgill.sel.ram.Aspect;
import ca.mcgill.sel.ram.RamPackage;
import ca.mcgill.sel.ram.provider.RamItemProviderAdapterFactory;
import ca.mcgill.sel.ram.ui.RamApp;
import ca.mcgill.sel.ram.ui.scenes.DisplayAspectScene;
import ca.mcgill.sel.ram.ui.views.message.handler.IMessageViewHandler;
import ca.mcgill.sel.ram.ui.views.message.handler.MessageViewHandlerFactory;
import ca.mcgill.sel.ram.util.RamResourceFactoryImpl;

/**
 * @author Wayne
 *
 */
public class MessageViewHandlerTest {

    private static Object waiter = new Object();
    private static volatile boolean mayProceed;
    
    private static MessageViewHandler handler;
    
    private static Aspect aspect;
    private String aspectLocation = "models/ecse429_test_models/TestModel03.ram";
    
    /**
     * Set up the resources and GUI.
     * @throws java.lang.Exception if something went wrong.
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Initialize ResourceManager.
        ResourceManager.initialize();
        // Initialize packages.
        RamPackage.eINSTANCE.eClass();
        
        // Register resource factories.
        ResourceManager.registerExtensionFactory("ram", new RamResourceFactoryImpl());
    
        // Initialize adapter factories.
        AdapterFactoryRegistry.INSTANCE.addAdapterFactory(RamItemProviderAdapterFactory.class);
        
        RamApp.initialize(new Runnable() {
            
            @Override
            public void run() {
                appNotify();
            }
        });
        
        // Wait for RamApp to be initialized.
        unitTestWait();
        handler = (MessageViewHandler) MessageViewHandlerFactory.INSTANCE.getMessageViewHandler();
    }

//    /**
//     * @throws java.lang.Exception if something went wrong.
//     */
//    @AfterClass
//    public static void tearDownAfterClass() throws Exception {
//    }

    /**
     * Loads aspect before test.
     * @throws java.lang.Exception if something went wrong.
     */
    @Before
    public void setUp() throws Exception {
        // Load model to use in test.
        aspect = (Aspect) ResourceManager.loadModel(aspectLocation);
        RamApp.getApplication().addSceneChangeListener(new ISceneChangeListener() {

            @Override
            public void processSceneChangeEvent(SceneChangeEvent event) {
                // Resume once the new aspect scene is loaded (switched to).
                if (event.getNewScene() instanceof DisplayAspectScene) {
                    RamApp.getApplication().removeSceneChangeListener(this);
                    appNotify();
                }
            }
        });

        RamApp.getApplication().loadAspect(aspect);

        // Wait for UI to be updated.
        unitTestWait();
    }

    /**
     * Unloads aspect after test case.
     * @throws java.lang.Exception if something went wrong.
     */
    @After
    public void tearDown() throws Exception {
        // Close current aspect.
        if (aspect != null) {
            RamApp.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                    RamApp.getApplication().closeAspectScene(RamApp.getActiveAspectScene());
                    ResourceManager.unloadResource(aspect.eResource());
                }
            });
        }
    }

    /**
     * Test method for {@link ca.mcgill.sel.ram.ui.views.message.handler.impl.MessageViewHandler
     * #processUnistrokeEvent(org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeEvent)}.
     */
    @Test
    public void testProcessUnistrokeEvent() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link ca.mcgill.sel.ram.ui.views.message.handler.impl.MessageViewHandler
     * #handleUnistrokeGesture(ca.mcgill.sel.ram.ui.views.AbstractView, 
     * org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeUtils.UnistrokeGesture, 
     * org.mt4j.util.math.Vector3D, org.mt4j.input.
     * inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeEvent)}.
     */
    @Test
    public void testHandleUnistrokeGesture() {
        handler.handleUnistrokeGesture(null, null, null, null);
        // Nothing to test here
    }

    /**
     * One single test case go through all paths.<br>
     * Test method for {@link ca.mcgill.sel.ram.ui.views.message.handler.impl.MessageViewHandler
     * #processWheelEvent(ca.mcgill.sel.ram.ui.events.WheelEvent)}.
     */
    @Test
    public void testProcessWheelEvent() {
        boolean processed = handler.processWheelEvent(null);
        assertTrue(processed);
    }

    /**
     * One single test case go through all paths. <br>
     * Test method for {@link ca.mcgill.sel.ram.ui.views.message.handler.impl.MessageViewHandler
     * #processZoomEvent(org.mt4j.input.inputProcessors.componentProcessors.zoomProcessor.ZoomEvent)}.
     */
    @Test
    public void testProcessZoomEvent() {
        boolean processed = handler.processZoomEvent(null);
        assertTrue(processed);
    }

    /**
     * Test method for {@link ca.mcgill.sel.ram.ui.views.message.handler.impl.MessageViewHandler
     * #handleCreateFragment(ca.mcgill.sel.ram.ui.views.message.MessageViewView, 
     * ca.mcgill.sel.ram.ui.views.message.LifelineView, 
     * org.mt4j.util.math.Vector3D, 
     * ca.mcgill.sel.ram.FragmentContainer)}.
     */
    @Test
    public void testHandleCreateFragment() {
        fail("Not yet implemented");
    }
    
    /**
     * Makes the Test suite wait for the UI updated.
     * @throws InterruptedException if interrupted
     */
    private static void unitTestWait() throws InterruptedException {
        synchronized (waiter) {
            while (!mayProceed) {
                waiter.wait();
            }
            mayProceed = false;
        }
    }
    
    /**
     * Makes the app notify the test suite to continue.
     */
    private static void appNotify() {
        synchronized (waiter) {
            waiter.notify();
            mayProceed = true;
        }
    }

}
