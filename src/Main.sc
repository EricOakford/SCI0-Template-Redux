;;; Sierra Script 1.0 - (do not remove this comment)
(script# 0)
(include game.sh)
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
	SCI0 0
	HandsOn 1
	HandsOff 2
	NormalEgo 3
	cls 4
	Btst 5
	Bset 6
	Bclr 7
	SolvePuzzle 8
	EgoDead	9
	PrintDontHaveIt 10
	PrintAlreadyDoneThat 11
	PrintNotCloseEnough 12
)

(local
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
	curTextColor
	curBackColor
	detailLevel
	theMusic
	soundFx
	musicChannels
	colorCount
	debugging
	isHandsOff
	cIcon
	[gameFlags 10]
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
	;Clear text from the screen
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
	(music number: sDeath play:)
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

(instance Test_Object of InvItem
	(properties
		name {Test Object}
		description {This is a test object.}
		owner 0
		view vTestItem
		loop 0
		cel 0
	)
)

(instance SCI0 of Game
	(properties)
	
	(method (init)
		(SysWindow color: vBLACK back: vWHITE)
		(= colorCount (Graph GDetect))
		(= systemWindow SysWindow)
		(super init:)
		(= musicChannels (DoSound NumVoices))
		(= cIcon deathIcon)
		(= ego egoObj)
		(= score 0)
		(= possibleScore 0)
		(= version {x.yyy.zzz})
		(= debugging TRUE)
		(User alterEgo: ego)
		(= showStyle HSHUTTER)
		(TheMenuBar init: draw: hide:)
		(StatusLine code: statusCode disable:)	;Don't show the status line at startup
		
		(if debugging
			(self setCursor: normalCursor (HaveMouse) 300 170)
		else
			(HandsOff)
			(self setCursor: normalCursor FALSE 350 200)
		)
		(inventory add:
			;Add your inventory items here. Make sure they are in the same order as the item list in GAME.SH.
				Test_Object
		)

		((= soundFx SFX) init: owner: self)
		((= theMusic music) init: owner: self)
;		(Print "It's alive!")	;test message to show that the Game instance initialized successfully
		(self newRoom: SPEEDTEST)
	)
	
	(method (newRoom)
		(super newRoom: &rest)
	)
	
	(method (startRoom roomNum &tmp temp0)
		;clean up dialog and dispose various scripts from heap
		(LoadMany FALSE FILE JUMP EXTRA WINDOW TIMER FOLLOW REVERSE)
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
			(saidEvent
				(cond
					((Said 'die') ;this shouldn't be in your game; it's just used to test the EgoDead procedure.
						(EgoDead "This has beea a test of the Emergency Death Broadcast System." #title {You're dead.} #icon vDeathSkull)
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