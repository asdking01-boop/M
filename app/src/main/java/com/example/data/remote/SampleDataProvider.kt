package com.example.data.remote

import com.example.data.model.Channel
import com.example.data.model.Episode
import com.example.data.model.EpgProgram
import com.example.data.model.Season
import com.example.data.model.Series
import com.example.data.model.VodMovie

object SampleDataProvider {

    val sampleChannels: List<Channel> = listOf(
        Channel(
            id = "ch_1",
            streamId = "101",
            name = "KRAS Cinema HD",
            streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            logoUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=400",
            category = "Movies",
            channelNum = "101",
            currentProgram = EpgProgram(
                title = "Interstellar Odyssey",
                description = "Sci-fi epic exploration across mysterious wormholes and distant solar systems.",
                startTime = "20:00",
                endTime = "22:45",
                progressPercent = 0.45f
            ),
            upcomingProgram = EpgProgram(
                title = "Midnight Thriller",
                description = "Deep suspense in the dark alleys of a futuristic metropolis.",
                startTime = "22:45",
                endTime = "01:00",
                isCurrent = false
            )
        ),
        Channel(
            id = "ch_2",
            streamId = "102",
            name = "KRAS Sports Ultra 4K",
            streamUrl = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
            logoUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=400",
            category = "Sports",
            channelNum = "102",
            currentProgram = EpgProgram(
                title = "Championship Finals: Live Match",
                description = "World Cup knockout stage live in HDR with multi-angle commentary.",
                startTime = "21:00",
                endTime = "23:00",
                progressPercent = 0.60f
            ),
            upcomingProgram = EpgProgram(
                title = "Sports Analysis & Highlights",
                description = "Post-match reactions, expert breakdown, and player interviews.",
                startTime = "23:00",
                endTime = "00:00",
                isCurrent = false
            )
        ),
        Channel(
            id = "ch_3",
            streamId = "103",
            name = "KRAS World News 24/7",
            streamUrl = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8",
            logoUrl = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=400",
            category = "News",
            channelNum = "103",
            currentProgram = EpgProgram(
                title = "Global News Hour Live",
                description = "Breaking headlines, international economics, and technological updates.",
                startTime = "21:30",
                endTime = "22:30",
                progressPercent = 0.20f
            ),
            upcomingProgram = EpgProgram(
                title = "Tech Frontiers Tomorrow",
                description = "The latest advancements in AI, space telescopes, and robotics.",
                startTime = "22:30",
                endTime = "23:00",
                isCurrent = false
            )
        ),
        Channel(
            id = "ch_4",
            streamId = "104",
            name = "KRAS Planet Documentary",
            streamUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
            logoUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=400",
            category = "Documentary",
            channelNum = "104",
            currentProgram = EpgProgram(
                title = "Deep Ocean Wonders",
                description = "Mysterious luminous creatures of the Mariana Trench captured in 4K.",
                startTime = "20:15",
                endTime = "21:45",
                progressPercent = 0.80f
            ),
            upcomingProgram = EpgProgram(
                title = "The Secret Life of Felines",
                description = "Wild big cats across the Serengeti grasslands.",
                startTime = "21:45",
                endTime = "23:00",
                isCurrent = false
            )
        ),
        Channel(
            id = "ch_5",
            streamId = "105",
            name = "KRAS Kids & Animation",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            logoUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=400",
            category = "Kids",
            channelNum = "105",
            currentProgram = EpgProgram(
                title = "Bunny Forest Adventure",
                description = "Fun animated adventures with cheerful woodland critters.",
                startTime = "21:00",
                endTime = "22:00",
                progressPercent = 0.35f
            ),
            upcomingProgram = EpgProgram(
                title = "Magic Kingdom Tales",
                description = "Fairytales and musical adventures for young dreamers.",
                startTime = "22:00",
                endTime = "22:45",
                isCurrent = false
            )
        ),
        Channel(
            id = "ch_6",
            streamId = "106",
            name = "KRAS Arena Action",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            logoUrl = "https://images.unsplash.com/photo-1517649763962-0c623266ddc0?w=400",
            category = "Sports",
            channelNum = "106",
            currentProgram = EpgProgram(
                title = "Extreme Motorsport GP",
                description = "High-octane rally racing through mountain turns and desert tracks.",
                startTime = "20:30",
                endTime = "22:30",
                progressPercent = 0.50f
            ),
            upcomingProgram = EpgProgram(
                title = "Pro MMA Showdown",
                description = "Bout highlights and champion title defense matches.",
                startTime = "22:30",
                endTime = "01:00",
                isCurrent = false
            )
        ),
        Channel(
            id = "ch_7",
            streamId = "107",
            name = "KRAS Premiere Plus",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            logoUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=400",
            category = "Entertainment",
            channelNum = "107",
            currentProgram = EpgProgram(
                title = "The Grand Stage: Live Concert",
                description = "Spectacular symphony and electronic visual performance.",
                startTime = "21:00",
                endTime = "23:30",
                progressPercent = 0.15f
            ),
            upcomingProgram = EpgProgram(
                title = "Late Night Comedy Show",
                description = "Celebrity interviews, sketches, and musical guests.",
                startTime = "23:30",
                endTime = "00:30",
                isCurrent = false
            )
        )
    )

    val sampleMovies: List<VodMovie> = listOf(
        VodMovie(
            id = "mov_1",
            streamId = "m_101",
            title = "Tears of Steel: Cybernetic Rebellion",
            streamUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
            posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600",
            backdropUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=1200",
            genre = "Sci-Fi / Action",
            year = "2024",
            duration = "1h 58m",
            durationSeconds = 7080L,
            rating = "9.1",
            description = "In a dystopian Amsterdam of the near future, a group of scientists and freedom fighters gather in the Old Church to reconstruct a lost past and fight back against autonomous biomechanical titans.",
            isFavorite = false
        ),
        VodMovie(
            id = "mov_2",
            streamId = "m_102",
            title = "Sintel: Quest for the Dragon",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600",
            backdropUrl = "https://images.unsplash.com/photo-1511447333015-45b65e60f6d5?w=1200",
            genre = "Fantasy / Adventure",
            year = "2024",
            duration = "1h 42m",
            durationSeconds = 6120L,
            rating = "8.8",
            description = "A lone young warrior traverses unforgiving snowy peaks and barren deserts in search of her kidnapped companion dragon Scales, discovering that loyalty holds perilous truths.",
            isFavorite = true
        ),
        VodMovie(
            id = "mov_3",
            streamId = "m_103",
            title = "Big Buck: Forest Revenge",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600",
            backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200",
            genre = "Animation / Comedy",
            year = "2023",
            duration = "1h 35m",
            durationSeconds = 5700L,
            rating = "8.5",
            description = "When three mischievous forest bullies disturb the peace of a gentle giant, he crafts clever and hilarious traps to reclaim harmony in the forest.",
            isFavorite = false
        ),
        VodMovie(
            id = "mov_4",
            streamId = "m_104",
            title = "Apex Protocol: Midnight Run",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600",
            backdropUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=1200",
            genre = "Thriller / Crime",
            year = "2024",
            duration = "2h 05m",
            durationSeconds = 7500L,
            rating = "8.9",
            description = "An undercover intelligence operative races across European capitals to defuse a rogue cryptographic payload capable of blacking out the continental electrical grid.",
            isFavorite = false
        ),
        VodMovie(
            id = "mov_5",
            streamId = "m_105",
            title = "Elephants Dream: Infinite Architecture",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            posterUrl = "https://images.unsplash.com/photo-1511447333015-45b65e60f6d5?w=600",
            backdropUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=1200",
            genre = "Mystery / Sci-Fi",
            year = "2023",
            duration = "1h 50m",
            durationSeconds = 6600L,
            rating = "8.7",
            description = "Two enigmatic travelers wander through a towering, ever-shifting mechanical labyrinth that alters its geometry according to their hidden psychological fears.",
            isFavorite = false
        ),
        VodMovie(
            id = "mov_6",
            streamId = "m_106",
            title = "The Horizon Drift",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            posterUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=600",
            backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200",
            genre = "Drama / Adventure",
            year = "2024",
            duration = "2h 12m",
            durationSeconds = 7920L,
            rating = "9.0",
            description = "An intrepid solo sailor encounters unexpected magnetic anomalies in the South Pacific, pushing human resilience to the ultimate test of courage.",
            isFavorite = false
        )
    )

    val sampleSeries: List<Series> = listOf(
        Series(
            id = "ser_1",
            seriesId = "s_201",
            title = "Chronicles of the Dark Nexus",
            posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600",
            backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200",
            genre = "Sci-Fi / Thriller",
            year = "2024",
            rating = "9.3",
            description = "A clandestine team of quantum temporal investigators tracks paradoxical anomalies across alternate timelines in modern Tokyo and New York.",
            seasons = listOf(
                Season(
                    seasonNum = 1,
                    title = "Season 1",
                    episodes = listOf(
                        Episode(
                            id = "ep_1_1",
                            seriesId = "ser_1",
                            seasonNum = 1,
                            episodeNum = 1,
                            title = "The Initial Wave",
                            streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                            posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400",
                            duration = "48m",
                            durationSeconds = 2880L,
                            description = "The particle accelerator triggers an unexplained pulse that fractures local spacetime."
                        ),
                        Episode(
                            id = "ep_1_2",
                            seriesId = "ser_1",
                            seasonNum = 1,
                            episodeNum = 2,
                            title = "Echoes in the Static",
                            streamUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
                            posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=400",
                            duration = "52m",
                            durationSeconds = 3120L,
                            description = "Encrypted communications from fifty years in the future begin broadcasting on radio frequencies."
                        ),
                        Episode(
                            id = "ep_1_3",
                            seriesId = "ser_1",
                            seasonNum = 1,
                            episodeNum = 3,
                            title = "Fractured Reality",
                            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                            posterUrl = "https://images.unsplash.com/photo-1511447333015-45b65e60f6d5?w=400",
                            duration = "46m",
                            durationSeconds = 2760L,
                            description = "The team discovers an identical copy of their research facility deep beneath the Alps."
                        ),
                        Episode(
                            id = "ep_1_4",
                            seriesId = "ser_1",
                            seasonNum = 1,
                            episodeNum = 4,
                            title = "Event Horizon Protocol",
                            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                            posterUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=400",
                            duration = "55m",
                            durationSeconds = 3300L,
                            description = "Season finale: A desperate standoff to prevent a temporal singularity from consuming the city."
                        )
                    )
                ),
                Season(
                    seasonNum = 2,
                    title = "Season 2",
                    episodes = listOf(
                        Episode(
                            id = "ep_2_1",
                            seriesId = "ser_1",
                            seasonNum = 2,
                            episodeNum = 1,
                            title = "Reconstructed Past",
                            streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                            posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400",
                            duration = "50m",
                            durationSeconds = 3000L,
                            description = "The consequences of the singularity emerge as memory itself becomes fluid."
                        )
                    )
                )
            ),
            isFavorite = true
        ),
        Series(
            id = "ser_2",
            seriesId = "s_202",
            title = "Shadow Sovereign",
            posterUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=600",
            backdropUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=1200",
            genre = "Crime / Political Drama",
            year = "2024",
            rating = "9.0",
            description = "Behind the glittering high-rises of a global financial capital lies an unseen syndicate dictating international treaties and currency values.",
            seasons = listOf(
                Season(
                    seasonNum = 1,
                    title = "Season 1",
                    episodes = listOf(
                        Episode(
                            id = "ep_s2_1",
                            seriesId = "ser_2",
                            seasonNum = 1,
                            episodeNum = 1,
                            title = "The Sovereign Vault",
                            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                            posterUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=400",
                            duration = "58m",
                            durationSeconds = 3480L,
                            description = "A billion-dollar transfer disappears without leaving a ledger entry."
                        ),
                        Episode(
                            id = "ep_s2_2",
                            seriesId = "ser_2",
                            seasonNum = 1,
                            episodeNum = 2,
                            title = "High Stakes Diplomacy",
                            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                            posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=400",
                            duration = "54m",
                            durationSeconds = 3240L,
                            description = "Private intelligence operatives play a high-risk game of deception during an international summit."
                        )
                    )
                )
            ),
            isFavorite = false
        )
    )
}
