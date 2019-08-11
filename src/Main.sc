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
	AnimateCast 1
	HandsOn 2
	HandsOff 3
	NormalEgo 4
	cls 5
	Btst 6
	Bset 7
	Bclr 8
	SolvePuzzle 9
	EgoDead	10
	PrintDontHaveIt 11
	PrintAlreadyDoneThat 12
	PrintNotCloseEnough 13
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
	curSaveDir
		global31
		global32
		global33
		global34
		global35
		global36
		global37
		global38
		global39
		global40
		global41
		global42
		global43
		global44
		global45
		global46
		global47
		global48
		global49
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
	musicChannels
	global103
	debugging	;debug mode enabled
	detailLevel		;detail level (0 = low, 1 = mid, 2 = high, 3 = ultra)
	theMusic			;music object, current playing music
	colorCount
	speedCount		;used to test how fast the system is
					;and used in determining detail level. (used in conjunction with detailLevel)
	cIcon
	soundFx				;sound effect being played
	[gameFlags 10]	;each global can have 16 flags. 10 globals * 16 flags = 160 flags. If you need more flags, just increase the array!
	curTextColor ;color of text in message boxes
	curBackColor ;color of message boxes
)

(procedure (AnimateCast)
	;Used to animate the cast, generally in a room's init() method.
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
		setCycle: Walk
		illegalBits: cWHITE
		cycleSpeed: 0
		moveSpeed: 0
		setStep: 3 2
		ignoreActors: 0
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

(procedure (SolvePuzzle flag points)
	;Adds an amount to the player's current score. A flag (one used with
	;Bset, Bclr, and Btst) is used so that a score is only added once.
		(if (not (Btst flag))
		(theGame changeScore: points)
		(Bset flag)
	)
)		

(procedure (EgoDead &tmp printRet)
	;This procedure handles when Ego dies. It closely matches that of QFG1EGA.
	;To use it: "(EgoDead {death message})".
	;You can add a title and icon in the same way as a normal Print message.
	(HandsOff)
	(Wait 100)
	(= normalCursor ARROW_CURSOR)
	(theGame setCursor: normalCursor TRUE)
	(soundFx stop:)
	(theMusic number: deathMusic play:)
		(repeat
			(= printRet
				(Print
					&rest
					#width 250
					#button	{Restore} 1
					#button {Restart} 2
					#button {__Quit__} 3
				)
			)
				(switch printRet
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
(procedure (PrintDontHaveIt)
	(Print "You don't have it.")
)

(procedure (PrintAlreadyDoneThat)
	(Print "You've already done that.")
)

(procedure (PrintNotCloseEnough)
	(Print "You're not close enough.")
)

(instance egoObj of Ego
	(properties
		name "ego"
	)
)

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
	)
)

(instance deathIcon of DCIcon
	(properties)
)

(instance SCI0 of Game ;Replace "SCI0 with the game's internal name here (up to 6 characters)
	; The main game instance. It adds game-specific functionality.
	(properties)
	
	(method (init)
		;set up various aspects of the game
		(super init:)
		(= cIcon deathIcon)
		(= ego egoObj)
		(= version {x.yyy.zzz})
		(User alterEgo: ego)
		(TheMenuBar init: draw: hide:)
		(StatusLine code: statusCode disable:) ;hide the status code at startup		
		((= theMusic music) number: sDeath owner: self init:)
		((= soundFx SFX) number: sDeath owner: self init:)
		(if debugging
			(self setCursor: normalCursor (HaveMouse) 300 170)
		else
			(HandsOff)
			(self setCursor: normalCursor FALSE 350 200)
		)					
		;moved inventory into its own script
		((ScriptID GAME_INV 0) init:)
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
	
	(method (newRoom)
		(super newRoom: &rest)
	)
	
	(method (startRoom roomNum)
		(LoadMany FALSE	
			;These are all disposed when going to another room, to reduce the
			;chances of "Memory Fragmented" errors.
			EXTRA FILE QSOUND GROOPER FORCOUNT SIGHT DPATH JUMP SMOOPER
			REVERSE CHASE FOLLOW WANDER EXTRA AVOIDER TIMER QSOUND
		)
		(cls)
		(if debugging
			(if
				(and
					;if memory is fragmented and debugging is on, bring up a warning and internal debugger
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
	
	(method (handleEvent event)
		(if (event claimed?)
			(return)
		)
		(super handleEvent: event)
		(switch (event type?)
		;Add global parser commands here.
			(saidEvent
				(cond
					((Said 'die') ;this shouldn't be in your game; it's just used to test the EgoDead procedure.
						(EgoDead "This has beea a test of the Emergency Death Broadcast System." #title {You're dead.} #icon vDeathSkull)
					)
					((Said 'cheat')
						(Print "Okay, you win.")
						(Print "(Game over.)" #at -1 152)
						(= quit TRUE)
					)
				)
			)
		)
		(if debugging
			(if
				(and
					(== (event type?) mouseDown)
					(& (event modifiers?) shiftDown)
				)
				(if (not (User canInput?))
					(event claimed: TRUE)
				else
					(cast eachElementDo: #handleEvent event)
					(if (event claimed?)
						(return)
					)
				)
			)
			(switch (event type?)
				(keyDown
					((ScriptID DEBUG) handleEvent: event)
				)
				(mouseDown
					((ScriptID DEBUG) handleEvent: event)
				)
			)
		)
	)
)