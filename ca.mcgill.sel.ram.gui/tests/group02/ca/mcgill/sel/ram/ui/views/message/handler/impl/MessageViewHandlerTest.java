package ca.mcgill.sel.ram.ui.views.message.handler.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mt4j.components.visibleComponents.shapes.MTPolygon;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputData.MTFingerInputEvt;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeEvent;
import org.mt4j.sceneManagement.ISceneChangeListener;
import org.mt4j.sceneManagement.SceneChangeEvent;
import org.mt4j.util.math.Vertex;

import ca.mcgill.sel.commons.emf.util.AdapterFactoryRegistry;
import ca.mcgill.sel.commons.emf.util.ResourceManager;
import ca.mcgill.sel.ram.Aspect;
import ca.mcgill.sel.ram.Classifier;
import ca.mcgill.sel.ram.Interaction;
import ca.mcgill.sel.ram.MessageView;
import ca.mcgill.sel.ram.Operation;
import ca.mcgill.sel.ram.RamPackage;
import ca.mcgill.sel.ram.provider.RamItemProviderAdapterFactory;
import ca.mcgill.sel.ram.ui.RamApp;
import ca.mcgill.sel.ram.ui.scenes.DisplayAspectScene;
import ca.mcgill.sel.ram.ui.views.message.MessageViewView;
import ca.mcgill.sel.ram.ui.views.message.handler.IMessageViewHandler;
import ca.mcgill.sel.ram.ui.views.message.handler.MessageViewHandlerFactory;
import ca.mcgill.sel.ram.util.RAMModelUtil;
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
     * Test case 1: Gesture is not started, ended nor updated. <br>
     * Test method for {@link ca.mcgill.sel.ram.ui.views.message.handler.impl.MessageViewHandler
     * #processUnistrokeEvent(org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeEvent)}.
     */
    @Test
    public void testProcessUnistrokeEvent01() {
        UnistrokeEvent event = new UnistrokeEvent(null, MTGestureEvent.GESTURE_CANCELED, null, null, null, null);
        handler.processUnistrokeEvent(event);
        
        // Nothing should have changed
    }
    
    /**
     * Test case 2: Gesture is started. <br>
     * @throws InterruptedException if interrupted
     * @see #testProcessUnistrokeEvent01()
     */
    @Test
    public void testProcessUnistrokeEvent02() throws InterruptedException {
        RamApp app = RamApp.getApplication();
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething1 = classA.getOperations().get(0);
        final MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething1);        
        final DisplayAspectScene aspectScene = (DisplayAspectScene) app.getCurrentScene();
        
        // Changes the scene
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                aspectScene.showMessageView(messageView);
                appNotify();
            }
        });
        unitTestWait();
        
        Vertex[] vertices = new Vertex[1];
        vertices[0] = new Vertex();
        MTPolygon visualization = new MTPolygon(app, vertices);
        MessageViewView mvv = (MessageViewView) aspectScene.getCurrentView();
        UnistrokeEvent event = new UnistrokeEvent(null, MTGestureEvent.GESTURE_STARTED, 
                mvv, visualization, null, null);
        handler.processUnistrokeEvent(event);
        
        // TODO: unistroke layer has one more child
    }
    
    /**
     * Test case 3: Gesture is updated but too short. <br>
     * @throws InterruptedException if interrupted.
     * @see #testProcessUnistrokeEvent01()
     */
    @Test
    public void testProcessUnistrokeEvent03() throws InterruptedException {
        RamApp app = RamApp.getApplication();
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething1 = classA.getOperations().get(0);
        final MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething1);        
        final DisplayAspectScene aspectScene = (DisplayAspectScene) app.getCurrentScene();
        
        // Changes the scene
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                aspectScene.showMessageView(messageView);
                appNotify();
            }
        });
        unitTestWait();
        
        MessageViewView mvv = (MessageViewView) aspectScene.getCurrentView();
        InputCursor cursor = new InputCursor();
        cursor.getEvents().add(new MTFingerInputEvt(null, 0, 0, 0, cursor));        
        cursor.getEvents().add(new MTFingerInputEvt(null, 1, 0, 0, cursor));        
        UnistrokeEvent event = new UnistrokeEvent(null, MTGestureEvent.GESTURE_UPDATED, 
                mvv, null, null, cursor);
        handler.processUnistrokeEvent(event);
        
        // TODO: Nothing should have happened
    }
    
    /**
     * Test case 4: Gesture is updated and good length. <br>
     * @throws InterruptedException if interrupted
     * @see #testProcessUnistrokeEvent01()
     */
    @Test
    public void testProcessUnistrokeEvent04() throws InterruptedException {
        RamApp app = RamApp.getApplication();
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething1 = classA.getOperations().get(0);
        final MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething1);        
        final DisplayAspectScene aspectScene = (DisplayAspectScene) app.getCurrentScene();
        
        // Changes the scene
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                aspectScene.showMessageView(messageView);
                appNotify();
            }
        });
        unitTestWait();
        
        MessageViewView mvv = (MessageViewView) aspectScene.getCurrentView();
        InputCursor cursor = new InputCursor();
        cursor.getEvents().add(new MTFingerInputEvt(null, 0, 0, 0, cursor));        
        cursor.getEvents().add(new MTFingerInputEvt(null, 6, 0, 0, cursor));        
        UnistrokeEvent event = new UnistrokeEvent(null, MTGestureEvent.GESTURE_UPDATED, 
                mvv, null, null, cursor);
        handler.processUnistrokeEvent(event);
        
        // Nothing happens, but lifeline highlighted maybe
        // TODO: don't know who to check that
    }
    
    /**
     * Test case 5: Gesture is ended and does not reaches a lifeline. <br>
     * @throws InterruptedException if interrupted
     * @see #testProcessUnistrokeEvent01()
     */
    @Test
    public void testProcessUnistrokeEvent05() throws InterruptedException {
        RamApp app = RamApp.getApplication();
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething1 = classA.getOperations().get(0);
        final MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething1);        
        final DisplayAspectScene aspectScene = (DisplayAspectScene) app.getCurrentScene();
        
        // Changes the scene
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                aspectScene.showMessageView(messageView);
                appNotify();
            }
        });
        unitTestWait();
        
        MessageViewView mvv = (MessageViewView) aspectScene.getCurrentView();
        InputCursor cursor = new InputCursor();
        cursor.getEvents().add(new MTFingerInputEvt(null, 160, 180, 0, cursor));        
        cursor.getEvents().add(new MTFingerInputEvt(null, 260, 180, 0, cursor));        
        final UnistrokeEvent event = new UnistrokeEvent(null, MTGestureEvent.GESTURE_ENDED, 
                mvv, null, null, cursor);
        
        // Process the event
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                handler.processUnistrokeEvent(event);
                appNotify();
            }
        });
        unitTestWait();
        
        // TODO: Added component in aspectScene
    }

    /**
     * Test case 6: Gesture is ended and reaches a lifeline. <br>
     * @throws InterruptedException if interrupted
     * @see #testProcessUnistrokeEvent01()
     */
    @Test
    public void testProcessUnistrokeEvent06() throws InterruptedException {
        RamApp app = RamApp.getApplication();
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething2 = classA.getOperations().get(1);
        final MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething2);        
        final DisplayAspectScene aspectScene = (DisplayAspectScene) app.getCurrentScene();
        
        // Changes the scene
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                aspectScene.showMessageView(messageView);
                appNotify();
            }
        });
        unitTestWait();
        
        MessageViewView mvv = (MessageViewView) aspectScene.getCurrentView();
        InputCursor cursor = new InputCursor();
        cursor.getEvents().add(new MTFingerInputEvt(null, 160, 230, 0, cursor));        
        cursor.getEvents().add(new MTFingerInputEvt(null, 390, 230, 0, cursor));        
        final UnistrokeEvent event = new UnistrokeEvent(null, MTGestureEvent.GESTURE_ENDED, 
                mvv, null, null, cursor);
        
        // Process the event
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                handler.processUnistrokeEvent(event);
                appNotify();
            }
        });
        unitTestWait();
        
        // TODO: Added component in aspectScene
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
