package hu.mktiti.tulkas.api.challenge;

import hu.mktiti.tulkas.api.GameResult;

public final class ChallengeResult implements GameResult {

    public final Long points;
    public final Long maxPoints;

    public ChallengeResult(final Long points, final Long maxPoints) {
        this.points = points;
        this.maxPoints = maxPoints;
    }

    public static ChallengeResult crash() {
        return new ChallengeResult(null, null);
    }

    public static ChallengeResult result(final long points) {
        return new ChallengeResult(points, null);
    }

    public static ChallengeResult result(final long points, final long maxPoints) {
        return new ChallengeResult(points, maxPoints);
    }

    public boolean hasBotCrashed() {
        return points == null;
    }

    public boolean isComplete() {
        return points != null && points.equals(maxPoints);
    }

    @Override
    public String toString() {
        if (hasBotCrashed()) {
            return "Crash";
        } else if (maxPoints == null) {
            return points + " points";
        } else {
            return points + "/" + maxPoints + " points";
        }
    }
}