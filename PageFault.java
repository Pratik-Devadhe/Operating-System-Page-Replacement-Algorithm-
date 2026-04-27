import java.util.*;

public class PageFault {

    static class Step {
        int[] frames;
        boolean hit;

        Step(int[] frames, boolean hit) {
            this.frames = frames.clone();
            this.hit = hit;
        }
    }

    static class Result {
        int faults = 0;
        List<Step> steps = new ArrayList<>();
        int[] sequence;

        String toJSON(String algo) {
            StringBuilder sb = new StringBuilder();

            sb.append("{\"algorithm\":\"").append(algo).append("\",");

            // sequence
            sb.append("\"sequence\":[");
            for (int i = 0; i < sequence.length; i++) {
                sb.append(sequence[i]);
                if (i < sequence.length - 1) sb.append(",");
            }
            sb.append("],");

            sb.append("\"faults\":").append(faults).append(",");
            sb.append("\"steps\":[");

            for (int i = 0; i < steps.size(); i++) {
                Step s = steps.get(i);
                sb.append("{\"frames\":[");

                for (int j = 0; j < s.frames.length; j++) {
                    sb.append(s.frames[j]);
                    if (j < s.frames.length - 1) sb.append(",");
                }

                sb.append("],\"hit\":").append(s.hit).append("}");

                if (i < steps.size() - 1) sb.append(",");
            }

            sb.append("]}");
            return sb.toString();
        }
    }

    public static void main(String[] args) {

        String[] seqStr = args[0].split(" ");
        int frames = Integer.parseInt(args[1]);
        String algo = args[2];

        int[] pages = Arrays.stream(seqStr).mapToInt(Integer::parseInt).toArray();

        Result res;

        switch (algo) {
            case "FIFO":
                res = fifo(pages, frames);
                break;
            case "LRU":
                res = lru(pages, frames);
                break;
            case "OPTIMAL":
                res = optimal(pages, frames);
                break;
            default:
                System.out.println("{\"error\":\"Invalid algorithm\"}");
                return;
        }

        res.sequence = pages;

        System.out.println(res.toJSON(algo));
    }

    // FIFO
    static Result fifo(int[] pages, int f) {
        Result res = new Result();
        Queue<Integer> q = new LinkedList<>();
        Set<Integer> set = new HashSet<>();
        int[] frames = new int[f];
        Arrays.fill(frames, -1);

        for (int page : pages) {
            boolean hit = set.contains(page);

            if (!hit) {
                res.faults++;

                if (set.size() == f) {
                    int removed = q.poll();
                    set.remove(removed);

                    for (int i = 0; i < f; i++) {
                        if (frames[i] == removed) {
                            frames[i] = page;
                            break;
                        }
                    }
                } else {
                    for (int i = 0; i < f; i++) {
                        if (frames[i] == -1) {
                            frames[i] = page;
                            break;
                        }
                    }
                }

                set.add(page);
                q.add(page);
            }

            res.steps.add(new Step(frames, hit));
        }

        return res;
    }

    // LRU
    static Result lru(int[] pages, int f) {
        Result res = new Result();
        int[] frames = new int[f];
        Arrays.fill(frames, -1);

        Map<Integer, Integer> lastUsed = new HashMap<>();

        for (int i = 0; i < pages.length; i++) {
            int page = pages[i];
            boolean hit = false;

            for (int j = 0; j < f; j++) {
                if (frames[j] == page) {
                    hit = true;
                    break;
                }
            }

            if (!hit) {
                res.faults++;

                int replaceIndex = -1;

                for (int j = 0; j < f; j++) {
                    if (frames[j] == -1) {
                        replaceIndex = j;
                        break;
                    }
                }

                if (replaceIndex == -1) {
                    int lruPage = -1, min = Integer.MAX_VALUE;

                    for (int j = 0; j < f; j++) {
                        int p = frames[j];
                        int used = lastUsed.getOrDefault(p, -1);

                        if (used < min) {
                            min = used;
                            lruPage = p;
                        }
                    }

                    for (int j = 0; j < f; j++) {
                        if (frames[j] == lruPage) {
                            replaceIndex = j;
                            break;
                        }
                    }
                }

                frames[replaceIndex] = page;
            }

            lastUsed.put(page, i);
            res.steps.add(new Step(frames, hit));
        }

        return res;
    }

    // OPTIMAL
    static Result optimal(int[] pages, int f) {
        Result res = new Result();
        int[] frames = new int[f];
        Arrays.fill(frames, -1);

        for (int i = 0; i < pages.length; i++) {
            int page = pages[i];
            boolean hit = false;

            for (int j = 0; j < f; j++) {
                if (frames[j] == page) {
                    hit = true;
                    break;
                }
            }

            if (!hit) {
                res.faults++;

                int replaceIndex = -1;

                for (int j = 0; j < f; j++) {
                    if (frames[j] == -1) {
                        replaceIndex = j;
                        break;
                    }
                }

                if (replaceIndex == -1) {
                    int farthest = -1;

                    for (int j = 0; j < f; j++) {
                        int p = frames[j];
                        int k;

                        for (k = i + 1; k < pages.length; k++) {
                            if (pages[k] == p) break;
                        }

                        if (k > farthest) {
                            farthest = k;
                            replaceIndex = j;
                        }
                    }
                }

                frames[replaceIndex] = page;
            }

            res.steps.add(new Step(frames, hit));
        }

        return res;
    }
}