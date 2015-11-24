package ca.mcgill.sel.ram.ui.views.message.handler.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.event.MouseEvent;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mt4j.components.MTComponent;
import org.mt4j.components.visibleComponents.shapes.MTPolygon;
import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputData.MTFingerInputEvt;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeEvent;
import org.mt4j.sceneManagement.ISceneChangeListener;
import org.mt4j.sceneManagement.SceneChangeEvent;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.math.Vertex;

import ca.mcgill.sel.commons.emf.util.AdapterFactoryRegistry;
import ca.mcgill.sel.commons.emf.util.ResourceManager;
import ca.mcgill.sel.ram.Aspect;
import ca.mcgill.sel.ram.Classifier;
import ca.mcgill.sel.ram.CombinedFragment;
import ca.mcgill.sel.ram.FragmentContainer;
import ca.mcgill.sel.ram.Interaction;
import ca.mcgill.sel.ram.MessageView;
import ca.mcgill.sel.ram.Operation;
import ca.mcgill.sel.ram.RamPackage;
import ca.mcgill.sel.ram.provider.RamItemProviderAdapterFactory;
import ca.mcgill.sel.ram.ui.RamApp;
import ca.mcgill.sel.ram.ui.scenes.DisplayAspectScene;
import ca.mcgill.sel.ram.ui.views.message.LifelineView;
import ca.mcgill.sel.ram.ui.views.message.MessageViewView;
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
        MTComponent topLayer = RamApp.getActiveAspectScene().getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();
        
        handler.processUnistrokeEvent(event);
        
        // Nothing should have changed (no message, lifeline added)
        assertEquals(previousChildCount, topLayer.getChildCount());
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
        
        int previousChildCount = mvv.getUnistrokeLayer().getChildCount();
        MTComponent topLayer = RamApp.getActiveAspectScene().getContainerLayer().getParent();
        int topLayerChildCount = topLayer.getChildCount();
        
        handler.processUnistrokeEvent(event);
        
        assertEquals(previousChildCount + 1, mvv.getUnistrokeLayer().getChildCount());
        assertEquals(topLayerChildCount, topLayer.getChildCount());
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
        
        MTComponent topLayer = RamApp.getActiveAspectScene().getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();
        
        handler.processUnistrokeEvent(event);
        
        // Nothing should have happened
        assertEquals(previousChildCount, topLayer.getChildCount());
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
        
        MTComponent topLayer = RamApp.getActiveAspectScene().getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();
        handler.processUnistrokeEvent(event);
        
        // Nothing happens, and no lifeline highlighted because there is only one lifeline
        assertEquals(previousChildCount, topLayer.getChildCount());
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
        int previousLifelineCount = messageView.getSpecification().getLifelines().size();
        
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
        
        MTComponent topLayer = RamApp.getActiveAspectScene().getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();
        
        // Process the event
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                handler.processUnistrokeEvent(event);
                appNotify();
            }
        });
        unitTestWait();

        // A selector should have popped up
        assertEquals(previousChildCount + 1, topLayer.getChildCount());
        
        // Try to click one of the options
        click(app, 275, 250);
        
        // Clicks the create method (which should increase lifeline count and pop-up disappears)
        click(app, 140, 120);
        
        waitNextRenderLoop(app);

        assertEquals(previousChildCount, topLayer.getChildCount());
        assertEquals(previousLifelineCount + 1, messageView.getSpecification().getLifelines().size());
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
        int previousMessageCount = messageView.getSpecification().getMessages().size();
        
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
        
        MTComponent topLayer = RamApp.getActiveAspectScene().getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();
        
        // Process the event
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                handler.processUnistrokeEvent(event);
                appNotify();
            }
        });
        unitTestWait();
        
        // A selector should have popped up
        assertEquals(previousChildCount + 1, topLayer.getChildCount());
        
        // Try to click one of the options (click on the destroy method)
        click(app, 400, 260);
        
        waitNextRenderLoop(app);

        // Pop up have disappeared, and message should have increased by 1
        assertEquals(previousChildCount, topLayer.getChildCount());
        assertEquals(previousMessageCount + 1, messageView.getSpecification().getMessages().size());
    }
    
    /**
     * Test case 7: Gesture is ended and reaches a lifeline that has no fragment. <br>
     * @throws InterruptedException if interrupted
     * @see #testProcessUnistrokeEvent01()
     */
    @Test
    public void testProcessUnistrokeEvent07() throws InterruptedException {
        RamApp app = RamApp.getApplication();
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething4 = classA.getOperations().get(3);
        final MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething4);        
        final DisplayAspectScene aspectScene = (DisplayAspectScene) app.getCurrentScene();
        int previousMessageCount = messageView.getSpecification().getMessages().size();
        
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
        cursor.getEvents().add(new MTFingerInputEvt(null, 470, 180, 0, cursor));        
        final UnistrokeEvent event = new UnistrokeEvent(null, MTGestureEvent.GESTURE_ENDED, 
                mvv, null, null, cursor);
        
        MTComponent topLayer = RamApp.getActiveAspectScene().getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();
        
        // Process the event
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                handler.processUnistrokeEvent(event);
                appNotify();
            }
        });
        unitTestWait();
        
        // A selector should have popped up
        assertEquals(previousChildCount + 1, topLayer.getChildCount());
        
        // Try to click one of the options (click on the destroy method)
        click(app, 480, 210);        
        waitNextRenderLoop(app);

        // Pop up have disappeared, and message should have increased by 1
        assertEquals(previousChildCount, topLayer.getChildCount());
        assertEquals(previousMessageCount + 1, messageView.getSpecification().getMessages().size());
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
     * Test case 1: Create fragment and click create statement. <br>
     * Test method for {@link ca.mcgill.sel.ram.ui.views.message.handler.impl.MessageViewHandler
     * #handleCreateFragment(ca.mcgill.sel.ram.ui.views.message.MessageViewView, 
     * ca.mcgill.sel.ram.ui.views.message.LifelineView, 
     * org.mt4j.util.math.Vector3D, 
     * ca.mcgill.sel.ram.FragmentContainer)}.
     * @throws InterruptedException if interrupted.
     */
    @Test
    public void testHandleCreateFragment01() throws InterruptedException {
        RamApp app = RamApp.getApplication();
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething1 = classA.getOperations().get(0);
        final MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething1);
        final Interaction owner = messageView.getSpecification();
        int previousFragmentCount = owner.getFragments().size();
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
        
        MTComponent topLayer = RamApp.getActiveAspectScene().getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();
        
        final MessageViewView mvv = (MessageViewView) aspectScene.getCurrentView();
        final LifelineView llv = mvv.getLifelineView(owner.getLifelines().get(0));
        
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                handler.handleCreateFragment(mvv, llv, new Vector3D(160, 180), owner);
                appNotify();
            }
        });
        
        unitTestWait();
        
        // A pop-up should appear
        assertEquals(previousChildCount + 1, topLayer.getChildCount());
        
        // Now try to click create statement
        click(app, 150, 180);
        waitNextRenderLoop(app);
        
        // Pop should disappear and fragment count increased by 1
        assertEquals(previousChildCount, topLayer.getChildCount());
        assertEquals(previousFragmentCount + 1, owner.getFragments().size());
    }
    
    /**
     * Test case 2: Create fragment and click create combined fragment.
     * @throws InterruptedException if interrupted 
     * @see #testHandleCreateFragment01()
     */
    @Test
    public void testHandleCreateFragment02() throws InterruptedException {
        RamApp app = RamApp.getApplication();
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething1 = classA.getOperations().get(0);
        final MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething1);
        final Interaction owner = messageView.getSpecification();
        int previousFragmentCount = owner.getFragments().size();
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
        
        MTComponent topLayer = RamApp.getActiveAspectScene().getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();
        
        final MessageViewView mvv = (MessageViewView) aspectScene.getCurrentView();
        final LifelineView llv = mvv.getLifelineView(owner.getLifelines().get(0));
        
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                handler.handleCreateFragment(mvv, llv, new Vector3D(160, 180), owner);
                appNotify();
            }
        });
        
        unitTestWait();
        
        // A pop-up should appear
        assertEquals(previousChildCount + 1, topLayer.getChildCount());
        
        // Now try to click create combined fragment
        click(app, 150, 210);
        waitNextRenderLoop(app);
        
        // Pop should disappear and fragment count increased by 1
        assertEquals(previousChildCount, topLayer.getChildCount());
        assertEquals(previousFragmentCount + 1, owner.getFragments().size());
    }
    
    /**
     * Test case 3: Create fragment and click create assignment.
     * @throws InterruptedException if interrupted 
     * @see #testHandleCreateFragment01()
     */
    @Test
    public void testHandleCreateFragment03() throws InterruptedException {
        RamApp app = RamApp.getApplication();
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething2 = classA.getOperations().get(1);
        final MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething2);
        final Interaction owner = messageView.getSpecification();
        int previousFragmentCount = owner.getFragments().size();
        
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
        
        MTComponent topLayer = RamApp.getActiveAspectScene().getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();
        
        final MessageViewView mvv = (MessageViewView) aspectScene.getCurrentView();
        final LifelineView llv = mvv.getLifelineView(owner.getLifelines().get(1));
        
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                handler.handleCreateFragment(mvv, llv, new Vector3D(400, 230), owner);
                appNotify();
            }
        });
        
        unitTestWait();
        
        // A pop-up should appear
        assertEquals(previousChildCount + 1, topLayer.getChildCount());
        
        // Now try to click create combined fragment
        click(app, 400, 200);
        waitNextRenderLoop(app);
        
        // Pop should disappear and fragment count increased by 1
        assertEquals(previousChildCount, topLayer.getChildCount());
        assertEquals(previousFragmentCount + 1, owner.getFragments().size());
    }
    
    /**
     * Test case 4: Create fragment and click create self message.
     * @throws InterruptedException if interrupted 
     * @see #testHandleCreateFragment01()
     */
    @Test
    public void testHandleCreateFragment04() throws InterruptedException {
        RamApp app = RamApp.getApplication();
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething3 = classA.getOperations().get(2);
        final MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething3);
        Interaction owner = messageView.getSpecification();
        CombinedFragment cf = (CombinedFragment) owner.getFragments().get(1);
        final FragmentContainer container = cf.getOperands().get(0);
        int previousMessageCount = owner.getMessages().size();
        int previousFragmentCount = container.getFragments().size();
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
        
        MTComponent topLayer = RamApp.getActiveAspectScene().getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();
        
        final MessageViewView mvv = (MessageViewView) aspectScene.getCurrentView();
        final LifelineView llv = mvv.getLifelineView(owner.getLifelines().get(0));
        
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                handler.handleCreateFragment(mvv, llv, new Vector3D(160, 230), container);
                appNotify();
            }
        });
        
        unitTestWait();
        
        // A pop-up should appear
        assertEquals(previousChildCount + 1, topLayer.getChildCount());
        
        // Now try to click create self message
        click(app, 160, 280);
        waitNextRenderLoop(app);
        
        // Pop up should show different options, but still here
        assertEquals(previousChildCount + 1, topLayer.getChildCount());
        
        // Now try to click a method
        click(app, 125, 125);
        waitNextRenderLoop(app);
        
        // Pop should disappear and fragment count increased by 2 (2 new message occurrence)
        // And message count by 1
        assertEquals(previousChildCount, topLayer.getChildCount());
        assertEquals(previousFragmentCount + 2, container.getFragments().size());
        assertEquals(previousMessageCount + 1, owner.getMessages().size());
    }
    
    /**
     * Test case 5: Create fragment and click create reply message.
     * @throws InterruptedException if interrupted 
     * @see #testHandleCreateFragment01()
     */
    @Test
    public void testHandleCreateFragment05() throws InterruptedException {
        RamApp app = RamApp.getApplication();
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething3 = classA.getOperations().get(2);
        final MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething3);
        Interaction owner = messageView.getSpecification();
        CombinedFragment cf = (CombinedFragment) owner.getFragments().get(1);
        final FragmentContainer container = cf.getOperands().get(0);
        int previousMessageCount = owner.getMessages().size();
        int previousFragmentCount = container.getFragments().size();
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
        
        MTComponent topLayer = RamApp.getActiveAspectScene().getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();
        
        final MessageViewView mvv = (MessageViewView) aspectScene.getCurrentView();
        final LifelineView llv = mvv.getLifelineView(owner.getLifelines().get(0));
        
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                handler.handleCreateFragment(mvv, llv, new Vector3D(160, 230), container);
                appNotify();
            }
        });
        
        unitTestWait();
        
        // A pop-up should appear
        assertEquals(previousChildCount + 1, topLayer.getChildCount());
        
        // Now try to click create reply message
        click(app, 160, 310);
        waitNextRenderLoop(app);
                
        // Pop should disappear and fragment count increased by 1
        // And message count by 1
        assertEquals(previousChildCount, topLayer.getChildCount());
        assertEquals(previousFragmentCount + 1, container.getFragments().size());
        assertEquals(previousMessageCount + 1, owner.getMessages().size());
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
    
    /**
     * Simulates a click.
     * @param app The app.
     * @param x The x position.
     * @param y The y position.
     * @throws InterruptedException if interrupted
     */
    private static void click(RamApp app, int x, int y) throws InterruptedException {
        app.dispatchEvent(new MouseEvent(app, MouseEvent.MOUSE_PRESSED, 0, 
                MouseEvent.BUTTON1_MASK, x, y, x, y, 1, false, MouseEvent.BUTTON1));
        app.dispatchEvent(new MouseEvent(app, MouseEvent.MOUSE_RELEASED, 0, 
                MouseEvent.BUTTON1_MASK, x, y, x, y, 1, false, MouseEvent.BUTTON1));
        
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                appNotify();
            }
        });
        
        // This waits for the click to actually happen
        unitTestWait();
    }
    
    /**
     * Waits for the next render loop.
     * @param app The app.
     * @throws InterruptedException if interrupted.
     */
    private static void waitNextRenderLoop(RamApp app) throws InterruptedException {
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                appNotify();
            }
        });
        
        // This waits for the next render loop
        unitTestWait();
    }
    
    /**************************************
     * EXTRA TEST CASES FOR HIGHER COVERAGE
     **************************************/
    /**
     * Extra test 1: Create fragment after a send event.
     * @throws InterruptedException if interrupted
     */
    @Test
    public void testHandleCreateFragment06() throws InterruptedException {
        RamApp app = RamApp.getApplication();
        Classifier classA = aspect.getStructuralView().getClasses().get(0);
        Operation doSomething5 = classA.getOperations().get(4);
        final MessageView messageView = RAMModelUtil.getMessageViewFor(aspect, doSomething5);
        Interaction owner = messageView.getSpecification();
        System.out.println(owner.getFragments());
        CombinedFragment cf = (CombinedFragment) owner.getFragments().get(3);
        final FragmentContainer container = cf.getOperands().get(0);
        System.out.println(container.getFragments());
        System.out.println(container.getFragments().get(6));
        CombinedFragment innerCf = (CombinedFragment) container.getFragments().get(6);
        final FragmentContainer innerContainer = innerCf.getOperands().get(0);
        System.out.println(innerContainer.getFragments().size());
        int previousMessageCount = owner.getMessages().size();
        int previousFragmentCount = container.getFragments().size();
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
        
        MTComponent topLayer = RamApp.getActiveAspectScene().getContainerLayer().getParent();
        int previousChildCount = topLayer.getChildCount();
        
        final MessageViewView mvv = (MessageViewView) aspectScene.getCurrentView();
        final LifelineView llv = mvv.getLifelineView(owner.getLifelines().get(0));
        
        app.invokeLater(new Runnable() {
            @Override
            public void run() {
                handler.handleCreateFragment(mvv, llv, new Vector3D(160, 595), container);
                appNotify();
            }
        });
        
        unitTestWait();
        // A pop-up should appear
        assertEquals(previousChildCount + 1, topLayer.getChildCount());
        
        // Now try to click create combined fragments
        click(app, 160, 630);
        waitNextRenderLoop(app);
                
        // Pop should disappear and fragment count increased by 1
        assertEquals(previousChildCount, topLayer.getChildCount());
        assertEquals(previousFragmentCount + 1, container.getFragments().size());
    }
}
