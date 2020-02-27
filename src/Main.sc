;;; Sierra Script 1.0 - (do not remove this comment)
;
;	MAIN.SC
;
;	This is the main game script. It contains the main game class, all the global variables, and
;	a number of useful procedures.
;
;

(script# MAIN)
(include game.sh) (include menu.sh)
(use Intrface)
(use DCIcon)
(use LoadMany)
(use StopWalk)
(use Window)
(use Sound)
(use Save)
(use Motion)
(use Game)
(use Invent)
(use User)
(use Menu)
(use System)

(public
	SCI0 0 ;Replace "SCI0" with the game's internal name here (up to 6 characters)
	RedrawCast 1
	HandsOn 2
	HandsOff 3
	NormalEgo 4
	cls 5
	Btst 6
	Bset 7
	Bclr 8
	SolvePuzzle 9
	EgoDead	10
	DontHave 11
	AlreadyDone 12
	NotClose 13
	CantDo 14
	CantSee 15
)

(local
	;refer to SYSTEM.SH for information on globals 0-99.
	ego
	theGame
	curRoom
	speed =  6
	quit
	cast
	regions
	timers
	sounds
	inventory
	addToPics
	curRoomNum
	prevRoomNum
	newRoomNum
	debugOn
	score
	possibleScore
	showStyle =  IRISOUT
	aniInterval
	theCursor
	normalCursor =  ARROW_CURSOR
	waitCursor =  HAND_CURSOR
	userFont =  USERFONT
	smallFont =  999
	lastEvent
	modelessDialog
	bigFont =  USERFONT
	volume =  12
	version =  {ego}
	locales
	[curSaveDir 20]
	aniThreshold =  10
	perspective
	features
	sortedFeatures
	useSortedFeatures
	demoScripts
	egoBlindSpot
	overlays =  -1
	doMotionCue
	systemWindow
	demoDialogTime =  3
	currentPalette
	modelessPort
	;globals 63-99 are unused
		global63
		global64
		global65
		global66
		global67
		global68
		global69
		global70
		global71
		global72
		global73
		global74
		global75
		global76
		global77
		global78
		global79
		global80
		global81
		global82
		global83
		global84
		global85
		global86
		global87
		global88
		global89
		global90
		global91
		global92
		global93
		global94
		global95
		global96
		global97
		global98
	lastSysGlobal
	;globals 100 and above are for game use
	isHandsOff
	deathMusic	= sDeath	;default death music
	numColors
	numVoices
	debugging		;debug mode enabled
	howFast			;machine speed level (0 = slow, 1 = medium, 2 = fast, 3 = fastest)
	machineSpeed	;used to test how fast the system is
					; and used in determining game speed. (used in conjunction with howFast)
	theMusic		;music object, current playing music
	soundFx			;sound effect being played
	cIcon			;global pointer to cycling icon
	[gameFlags 10]	;each global can have 16 flags. 10 globals * 16 flags = 160 flags.
					; If you need more flags, just increase the array!
	myTextColor		;color of text in message boxes
	myBackColor		;color of message boxes
	egoWalk			;pointer for ego's Walk object
	egoStopWalk		;pointer for ego's StopWalk object
)

(procedure (RedrawCast)
	;Used to re-animate the cast without cycling
	(Animate (cast elements?) FALSE)
)

(procedure (HandsOn)
	;Enable ego control
	(= isHandsOff FALSE)
	(User canControl: TRUE canInput: TRUE)
	(theGame setCursor: normalCursor (HaveMouse))
)

(procedure (HandsOff)
	;Disable ego control
	(= isHandsOff TRUE)
	(User canControl: FALSE canInput: FALSE)
	(theGame setCursor: waitCursor TRUE)
	(ego setMotion: 0)
)

(procedure (NormalEgo)
	;normalizes ego's animation
	(ego
		setLoop: -1
		setPri: -1
		setMotion: 0
		setCycle: egoWalk
		illegalBits: cWHITE
		cycleSpeed: 0
		moveSpeed: 0
		setStep: 3 2
		ignoreActors: FALSE
		looper: 0
	)
)

(procedure (cls)
	;Clear modeless dialog from the screen
	(if modelessDialog (modelessDialog dispose:))
)

(procedure (Btst flagEnum)
	;Test a boolean game flag
	(& [gameFlags (/ flagEnum 16)] (>> $8000 (mod flagEnum 16)))
)

(procedure (Bset flagEnum  &tmp oldState)
	;Set a boolean game flag
	(= oldState (Btst flagEnum))
	(|= [gameFlags (/ flagEnum 16)] (>> $8000 (mod flagEnum 16)))
	oldState
)

(procedure (Bclr flagEnum  &tmp oldState)
	;Clear a boolean game flag
	(= oldState (Btst flagEnum))
	(&= [gameFlags (/ flagEnum 16)] (~ (>> $8000 (mod flagEnum 16))))
	oldState
)

(procedure (SolvePuzzle flagEnum points)
	;Adds an amount to the player's current score. A flag (one used with
	;Bset, Bclr, and Btst) is used so that a score is only added once.
		(if (not (Btst flagEnum))
		(theGame changeScore: points)
		(Bset flagEnum)
	)
)		

(procedure (EgoDead)
	;This procedure handles when Ego dies. It closely matches that of QFG1EGA.
	;It's used in the same way as a normal Print message.
	(HandsOff)
	(Wait 100)
	(= normalCursor ARROW_CURSOR)
	(theGame setCursor: normalCursor TRUE)
	(soundFx stop:)
	(theMusic number: deathMusic play:)
	(repeat
		(switch
			(Print
				&rest
				#width 250
				#button	{Restore} 1
				#button {Restart} 2
				#button {__Quit__} 3
			)
			(1
				(theGame restore:)
			)
			(2
				(theGame restart:)
			)
			(3
				(= quit TRUE) (break)
			)
		)
	)
)
(procedure (DontHave)
	(Print "You don't have it.")
)

(procedure (AlreadyDone)
	(Print "You've already done that.")
)

(procedure (NotClose)
	(Print "You're not close enough.")
)

(procedure (CantDo)
	(Print "You can't do that now.")
)

(procedure (CantSee)
	(Print "You see nothing like that here.")
)

(instance egoObj of Ego
	(properties
		name "ego"
	)
)

(instance egoW of Walk)

(instance egoSW of StopWalk)

(instance statusCode of Code
	(properties)
	
	(method (doit strg)
		(Format strg "___Template Game_______________Score: %d of %d" score possibleScore)
	)
)

(instance music of Sound
	(properties
		number sDeath
	)
)

(instance SFX of Sound
	(properties
		number sDeath
		priority 15
	)
)

(instance deathIcon of DCIcon
	(properties)
)

(instance SCI0 of Game ;Replace "SCI0 with the game's internal name here (up to 6 characters)
	; The main game instance. It adds game-specific functionality.
	(properties
		;Set your game's language here.
		;Supported langauges can be found in SYSTEM.SH.		
		parseLang ENGLISH
		printLang ENGLISH
	)
	
	(method (init)
		;load some important modules
		Cycle
		StopWalk
		Window
		DCIcon
		TheMenuBar
		;set up various aspects of the game
		(super init:)
		(= cIcon deathIcon)
		(= ego egoObj)
		(= egoWalk egoW)
		(= egoStopWalk egoSW)
		(= version {x.yyy.zzz}) ;set game version here
		(User alterEgo: ego)
		(TheMenuBar init: draw: hide: state: FALSE)
		(StatusLine code: statusCode disable:) ;hide the status line at startup
		(if debugging
			(self setCursor: normalCursor (HaveMouse) 300 170)
		else
			(HandsOff)
			(self setCursor: normalCursor FALSE 350 200)
		)
		((= theMusic music) number: sDeath owner: self init:)
		((= soundFx SFX) number: sDeath owner: self init:)
		(inventory add:
			;Add your inventory items here. Make sure they are in the same order as the item list in GAME.SH.
			Test_Object
		)
		;moved any code not requiring any objects in this script into its own script
		((ScriptID GAME_INIT 0) init:)
		;and finally, now that the game's been initialized, we can move on to the speed tester.
		(self newRoom: SPEEDTEST)
	)

	(method (doit)
		(super doit:)
	)

	(method (replay)
		(TheMenuBar draw:)
		(StatusLine enable:)
		(SetMenu soundI p_text
			(if (DoSound SoundOn) {Sound off} else {Sound on})
		)
		(super replay:)
	)	
	
	(method (startRoom roomNum)
		((ScriptID DISPOSE_CODE 0) doit:)
		(cls)
		(if debugging
			(if
				(and
					;if memory is fragmented and debugging is on, bring up a warning and the internal debugger
					(u> (MemoryInfo FreeHeap) (+ 20 (MemoryInfo LargestPtr)))
					(Print
						"Memory fragmented."
						#button {Debug} TRUE
					)
				)
				(SetDebug)
			)
			(User canInput: TRUE)
		)
		(NormalEgo)
		(super startRoom: roomNum)
	)
	
	(method (handleEvent event &tmp i)
		(super handleEvent: event)
		(if debugging
			(switch (event type?)
				(keyDown
					((ScriptID DEBUG) handleEvent: event)
				)
				(mouseDown
					((ScriptID DEBUG) handleEvent: event)
				)
			)
		)
		(switch (event type?)
		;Add global parser commands here.
			(saidEvent
				(cond
					((Said 'cheat')
						(Print "Okay, you win.")
						(Print "(Game over.)" #at -1 152)
						(= quit TRUE)
					)
					((Said 'look[<at]>') ;look at inventory items
						(if (= i (inventory saidMe:))
							(if (i ownedBy: ego)
								(i showSelf:)
								else (DontHave)
							)
							;if not an inventory item
						else ;this will handle "look anyword"
							(CantSee)
						)
					)
				)
			)
		)
	)
)

(class GameInvItem of InvItem
	;this subclass will allow item descriptions to be called
	;from TEXT.003 (item descriptions)
	(method (showSelf)
		(Print INVDESC description
			#title name
			#icon view 0 0
		)
	)
)

;add inventory items here

(instance Test_Object of GameInvItem
	(properties
		name {Test Object}
		said '/object'
		owner 0
		view vTestObject
		loop 0
		cel 0
	)
)
